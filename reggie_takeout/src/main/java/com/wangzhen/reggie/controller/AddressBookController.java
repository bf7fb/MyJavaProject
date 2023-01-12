package com.wangzhen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wangzhen.reggie.common.BaseContext;
import com.wangzhen.reggie.common.Result;
import com.wangzhen.reggie.pojo.AddressBook;
import com.wangzhen.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wz
 * @ClassName AddressBookController
 * @date 2023/1/8 15:01
 * @Description TODO
 */
@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {
        @Autowired
        private AddressBookService addressBookService;

        /**
         * 新增地址
         */
        @PostMapping
        public Result<AddressBook> save(@RequestBody AddressBook addressBook) {
            addressBook.setUserId(BaseContext.getCurrentId());
            log.info("addressBook:{}", addressBook);
            addressBookService.save(addressBook);
            return Result.success(addressBook);
        }

        /**
         * 设置默认地址
         */
        @PutMapping("default")
        public Result<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
            log.info("addressBook:{}", addressBook);
            LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
            wrapper.set(AddressBook::getIsDefault, 0);
            //SQL:update address_book set is_default = 0 where user_id = ?
            addressBookService.update(wrapper);

            addressBook.setIsDefault(1);
            //SQL:update address_book set is_default = 1 where id = ?
            addressBookService.updateById(addressBook);
            return Result.success(addressBook);
        }

        /**
         * 根据id查询地址
         */
        @GetMapping("/{id}")
        public Result get(@PathVariable Long id) {
            AddressBook addressBook = addressBookService.getById(id);
            if (addressBook != null) {
                return Result.success(addressBook);
            } else {
                return Result.error("没有找到该对象");
            }
        }

        /**
         * 下单的时候需要用到 查询默认地址 使用默认地址配送
         * 查询默认地址
         */
        @GetMapping("default")
        public Result<AddressBook> getDefault() {
            LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
            queryWrapper.eq(AddressBook::getIsDefault, 1);

            //SQL:select * from address_book where user_id = ? and is_default = 1
            AddressBook addressBook = addressBookService.getOne(queryWrapper);

            if (null == addressBook) {
                return Result.error("没有找到该对象");
            } else {
                return Result.success(addressBook);
            }
        }

        /**
         * 查询指定用户的全部地址
         */
        @GetMapping("/list")
        public Result<List<AddressBook>> list(AddressBook addressBook) {
            addressBook.setUserId(BaseContext.getCurrentId());
            log.info("addressBook:{}", addressBook);

            //条件构造器
            LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
            queryWrapper.orderByDesc(AddressBook::getUpdateTime);

            //SQL:select * from address_book where user_id = ? order by update_time desc
            return Result.success(addressBookService.list(queryWrapper));
        }

        /**
        * 修改地址
        * @param addressBook
        * @return
        */
        @PutMapping
        public Result<String> updateAddressBook(@RequestBody AddressBook addressBook){
            System.out.println(addressBook);
            addressBookService.updateById(addressBook);
            return Result.success("修改成功~");
        }

    /**
     * 删除地址
     * @param ids
     * @return
     */
    @DeleteMapping
        public Result<String> deleteAddressBook(Long ids){
            System.out.println("ids====" + ids);
            addressBookService.removeById(ids);
            return Result.success("删除成功~");
        }

    }

