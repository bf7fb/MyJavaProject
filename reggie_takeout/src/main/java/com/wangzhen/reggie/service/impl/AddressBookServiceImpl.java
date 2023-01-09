package com.wangzhen.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangzhen.reggie.mapper.AddressBookMapper;
import com.wangzhen.reggie.pojo.AddressBook;
import com.wangzhen.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wz
 * @ClassName AddressBookServiceImpl
 * @date 2023/1/8 15:00
 * @Description TODO
 */
@Service
@Slf4j
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook>
        implements AddressBookService {
}
