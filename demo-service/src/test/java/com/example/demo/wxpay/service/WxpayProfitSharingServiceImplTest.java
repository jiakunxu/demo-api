package com.example.demo.wxpay.service;

import com.alibaba.fastjson2.JSON;
import com.example.demo.wxpay.api.*;
import com.example.demo.wxpay.api.bo.*;
import com.example.demo.wxpay.dao.dataobject.*;
import com.example.demo.wxpay.dao.mapper.*;
import com.example.demo.support.ServiceTestSupport;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wechat.pay.java.service.profitsharing.model.OrdersEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WxpayProfitSharingServiceImplTest {
    private final ProfitsharingService remote = mock(ProfitsharingService.class);
    private final WxpayProfitSharingServiceImpl service = spy(new WxpayProfitSharingServiceImpl());
    private final RSAAutoCertificateConfig config = mock(RSAAutoCertificateConfig.class);
    @BeforeEach
    void initialize() { ServiceTestSupport.mapper(service, mock(WxpayProfitSharingMapper.class), WxpayProfitSharingDO.class);
    ReflectionTestUtils.setField(service, "profitsharingService", remote);
    ReflectionTestUtils.setField(service, "partnerConfig", config);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void queryMapsOrderStateAndMerchantMode(boolean subMerchant) {
        OrdersEntity response = JSON.parseObject("{\"order_id\":\"order-id\",\"transaction_id\":\"transaction\",\"out_order_no\":\"order\",\"sub_mchid\":\"sub\",\"state\":\"FINISHED\",\"receivers\":[]}", OrdersEntity.class);
        if (subMerchant) when(remote.queryOrder("sub", "transaction", "order")).thenReturn(response);
        else when(remote.queryOrder("transaction", "order")).thenReturn(response);
        var result = subMerchant ? service.getProfitSharing("sub", "transaction", "order") : service.getProfitSharing("transaction", "order");
        assertThat(result.getOrderId()).isEqualTo("order-id");
        assertThat(result.getState()).isEqualTo("FINISHED");
        assertThat(result.getReceivers()).isEqualTo("[]");
        assertThat(service.getProfitSharing("", "order")).isNull();
        assertThat(service.getProfitSharing("", "transaction", "order")).isNull();
    }

    @Test
    void callbackMapsOptionalMerchantIdentifiersAndFinishedState() {
        Map<String, String> data = Map.of("order_id", "id", "transaction_id", "transaction", "out_order_no", "order", "receiver", "[]", "success_time", "time", "sp_mchid", "sp", "sub_mchid", "sub");
        try (var parsers = mockConstruction(NotificationParser.class, (parser, context) -> { assertThat((Object[]) context.arguments().getFirst()).containsExactly(config); when(parser.parse(any(RequestParam.class), eq(Map.class))).thenReturn(data); })) {
            var result = service.getProfitSharing("serial", "nonce", "123", "sign", "body");
            assertThat(result.getOrderId()).isEqualTo("id");
            assertThat(result.getMchid()).isNull();
            assertThat(result.getSpMchid()).isEqualTo("sp");
            assertThat(result.getSubMchid()).isEqualTo("sub");
            assertThat(result.getState()).isEqualTo(WxpayProfitSharing.State.FINISHED.value);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateScopesOrderAndInsertsOnlyWhenNoRowsUpdated(boolean updated) {
        doReturn(updated).when(service).update(any(Wrapper.class));
        doReturn(true).when(service).save(any(WxpayProfitSharingDO.class));
        WxpayProfitSharing data = new WxpayProfitSharing();
        data.setState("FINISHED");
        data.setTransactionId("transaction");
        assertThat(service.updateProfitSharing("id", data)).isSameAs(data);
        ArgumentCaptor<Wrapper<WxpayProfitSharingDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var wrapper = (LambdaUpdateWrapper<WxpayProfitSharingDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("order_id");
        assertThat(wrapper.getSqlSet()).contains("state", "transaction_id").doesNotContain("mchid");
        assertThat(wrapper.getParamNameValuePairs().values()).contains("id", "FINISHED", "transaction");
        if (updated) verify(service, never()).save(any(WxpayProfitSharingDO.class));
        else verify(service).save(argThat(row -> "id".equals(row.getOrderId())));
    }

    @Test
    void insertAndValidationRejectMissingData() {
        doReturn(true).when(service).save(any(WxpayProfitSharingDO.class));
        WxpayProfitSharing data = new WxpayProfitSharing(); data.setOrderId("id");
        assertThat(service.insertProfitSharing(data)).isSameAs(data);
        verify(service).save(argThat(row -> "id".equals(row.getOrderId())));
        assertThatThrownBy(() -> service.insertProfitSharing(null)).hasMessage("参数信息不能为空");
        assertThatThrownBy(() -> service.updateProfitSharing("", data)).hasMessage("参数信息不能为空");
        assertThatThrownBy(() -> service.updateProfitSharing("id", null)).hasMessage("参数信息不能为空");
    }

    @ParameterizedTest
    @CsvSource({"false,missing", "true,missing", "false,null", "true,null",
        "false,empty", "true,empty", "false,populated", "true,populated"})
    void queriedReceiversPreserveNullAndControlUpdates(boolean subMerchant, String shape) {
        String field = switch (shape) {
            case "missing" -> "";
            case "null" -> ",\"receivers\":null";
            case "empty" -> ",\"receivers\":[]";
            default -> ",\"receivers\":[{\"account\":\"receiver\"}]";
        };
        var response = JSON.parseObject("{\"order_id\":\"id\",\"state\":\"FINISHED\""
            + field + "}", OrdersEntity.class);
        if (subMerchant) {
            when(remote.queryOrder("sub", "transaction", "order")).thenReturn(response);
        } else {
            when(remote.queryOrder("transaction", "order")).thenReturn(response);
        }
        WxpayProfitSharing result = subMerchant
            ? service.getProfitSharing("sub", "transaction", "order")
            : service.getProfitSharing("transaction", "order");
        boolean absent = shape.equals("missing") || shape.equals("null");
        if (absent) {
            assertThat(result.getReceivers()).isNull();
        } else {
            assertThat(JSON.parseArray(result.getReceivers()))
                .hasSize(shape.equals("empty") ? 0 : 1);
        }
        doReturn(true).when(service).update(any(Wrapper.class));
        service.updateProfitSharing("id", result);
        ArgumentCaptor<Wrapper<WxpayProfitSharingDO>> capture = ArgumentCaptor.captor();
        verify(service).update(capture.capture());
        var wrapper = (LambdaUpdateWrapper<WxpayProfitSharingDO>) capture.getValue();
        if (absent) {
            assertThat(wrapper.getSqlSet()).doesNotContain("receivers");
        } else {
            assertThat(wrapper.getSqlSet()).contains("receivers=");
            assertThat(wrapper.getParamNameValuePairs().values()).contains(result.getReceivers());
        }
        verify(service, never()).save(any(WxpayProfitSharingDO.class));
    }
}
