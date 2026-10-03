package com.example.demo.corp.service;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.corp.api.CorpRoleService;
import com.example.demo.corp.dao.dataobject.CorpRoleDO;
import com.example.demo.corp.dao.mapper.CorpRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CorpRoleServiceImpl extends ServiceImpl<CorpRoleMapper, CorpRoleDO>
                                 implements CorpRoleService {

}
