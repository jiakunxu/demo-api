package com.example.demo.config.api;

import com.example.demo.config.api.bo.Config;

import java.math.BigInteger;
import java.util.List;

/**
 * @author JiakunXu
 */
public interface ConfigService {

    long countConfig(Config config);

    List<Config> listConfigs(Config config);

    Config getConfig(String id, String key);

    Config insertConfig(Config config, String creator);

    Config updateConfig(BigInteger id, Config config, String modifier);

    Config deleteConfig(BigInteger id, String modifier);

}
