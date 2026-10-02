package com.example.demo.dict.api;

import com.example.demo.dict.api.bo.DictData;

import java.math.BigInteger;
import java.util.List;

/**
 * @author JiakunXu
 */
public interface DictDataService {

    long countData(String typeId, String typeValue, DictData data);

    List<DictData> listDatas(String typeId, String typeValue);

    List<DictData> listDatas(String typeId, String[] typeValue);

    List<DictData> listDatas(String typeId, String typeValue, DictData data);

    List<DictData> listDatas(String typeId, String[] typeValue, DictData data);

    DictData getData(String id);

    DictData getData(BigInteger id);

    DictData getData(String typeValue, String value);

    DictData insertData(BigInteger typeId, DictData data, String creator);

    DictData updateData(BigInteger id, DictData data, String modifier);

    DictData updateData(BigInteger typeId, String typeValue, String modifier);

    DictData deleteData(BigInteger typeId, BigInteger id, String modifier);

}
