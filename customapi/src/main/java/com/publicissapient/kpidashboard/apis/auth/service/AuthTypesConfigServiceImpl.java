package com.publicissapient.kpidashboard.apis.auth.service;

import com.publicissapient.kpidashboard.apis.activedirectory.service.ADServerDetailsService;
import com.publicissapient.kpidashboard.apis.auth.exceptions.InvalidAuthTypeConfigException;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeConfig;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.model.application.ValidationMessage;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.RsaEncryptionService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class AuthTypesConfigServiceImpl implements AuthTypesConfigService {

    @Autowired
    private ADServerDetailsService adServerDetailsService;

    @Autowired
    private GlobalConfigRepository globalConfigRepository;

    @Autowired
    private RsaEncryptionService rsaEncryptionService;

    @Autowired
    private AesEncryptionService aesEncryptionService;

    @Autowired
    private CustomApiConfig customApiConfig;

    @Autowired
    private AuthTypeConfigValidator authTypeConfigValidator;


    @Override
    public AuthTypeConfig saveAuthTypeConfig(AuthTypeConfig authTypeConfig) {

        ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);
        if (validationMessage.isValid()) {

            AuthTypeStatus authTypeStatus = authTypeConfig.getAuthTypeStatus();

            List<GlobalConfig> globalConfigs = globalConfigRepository.findAll();
            globalConfigs.get(0).setAuthTypeStatus(authTypeStatus);
            if (authTypeConfig.getAuthTypeStatus().isAdLogin()) {
                ADServerDetail adServerDetail = authTypeConfig.getAdServerDetail();
                String passEncrypt = encryptStringForDb(adServerDetail.getPassword());
                adServerDetail.setPassword(passEncrypt);
                globalConfigs.get(0).setAdServerDetail(adServerDetail);
            }

            globalConfigRepository.saveAll(globalConfigs);
        } else {
            throw new InvalidAuthTypeConfigException(validationMessage.getMessage());
        }

        return authTypeConfig;
    }

    @Override
    public AuthTypeConfig getAuthTypeConfig() {
        GlobalConfig globalConfig = getGlobalConfig();

        AuthTypeConfig authTypeConfig = new AuthTypeConfig();

        if (globalConfig != null) {
            authTypeConfig.setAdServerDetail(globalConfig.getAdServerDetail());
            authTypeConfig.setAuthTypeStatus(globalConfig.getAuthTypeStatus());
        }

        return authTypeConfig;
    }


    @Override
    public AuthTypeStatus getAuthTypesStatus() {
        GlobalConfig globalConfig = getGlobalConfig();
        return globalConfig != null ? globalConfig.getAuthTypeStatus() : null;
    }

    private GlobalConfig getGlobalConfig() {
        List<GlobalConfig> globalConfigs = globalConfigRepository.findAll();
        GlobalConfig globalConfig = CollectionUtils.isEmpty(globalConfigs) ? null : globalConfigs.get(0);
        return globalConfig;
    }

    private String encryptStringForDb(String rasEncryptedStringFromClient) {
        String plainText = rsaEncryptionService.decrypt(rasEncryptedStringFromClient,
                customApiConfig.getRsaPrivateKey());
        String encryptedString = aesEncryptionService.encrypt(plainText, customApiConfig.getAesEncryptionKey());
        return encryptedString == null ? "" : encryptedString;
    }
}
