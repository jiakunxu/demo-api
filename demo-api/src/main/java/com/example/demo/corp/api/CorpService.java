package com.example.demo.corp.api;

import com.example.demo.corp.api.bo.Corp;

import java.math.BigInteger;
import java.util.List;

/**
 * @author JiakunXu
 */
public interface CorpService {

    long countCorp(Corp corp);

    List<Corp> listCorps(Corp corp);

    Corp getCorp(String id);

    Corp getCorp(BigInteger id);

    Corp updateCorp(BigInteger id, Corp corp, String modifier);

}
