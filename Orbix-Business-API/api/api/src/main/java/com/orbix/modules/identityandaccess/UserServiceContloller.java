package com.orbix.modules.identityandaccess;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.modules.adminunits.SystemProfileServiceController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceContloller implements UserService {

}
