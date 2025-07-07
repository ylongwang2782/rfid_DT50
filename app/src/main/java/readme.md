# TODO
- [ ]完成写标签接口
- [ ]完成apk打包，并验证安装
- [ ]完成系统back键返回上一页，而非上一个程序
- [ ]build自动命名

# RFID EPC 写入功能改进说明

## 问题描述
原始程序在写入EPC数据时，只写入EPC内容，但不更新PC字段的EPC长度信息。这导致当写入更长的EPC数据时，RFID标签无法正确识别新的EPC长度，从而无法写入更长的EPC数据。

## 解决方案
在 `TagManageFragment.java` 中实现了以下改进：

### 1. 改进的PC值计算
```java
private String getPC(String epc){
    // EPC长度以字(word)为单位，每个字是2字节(4个十六进制字符)
    int len = epc.length()/4;
    // PC字段的EPC长度信息在bit[15:11]，需要左移11位
    int b = len << 11;
    String aHex = Integer.toHexString(b).toUpperCase();
    
    // 确保PC值是4位十六进制数
    while (aHex.length() < 4) {
        aHex = "0" + aHex;
    }
    
    return aHex;
}
```

### 2. 智能EPC写入功能
新增了 `writeEpcWithPcUpdate` 方法，采用以下策略：

#### 策略1：优先使用专用EPC写入方法
- 对于地址0或1的EPC写入，首先尝试使用 `mRfidManager.writeEpcString()` 方法
- 该方法由RFID SDK提供，会自动处理PC字段更新
- 兼容性最好，成功率最高

#### 策略2：手动分步写入（备用方案）
如果专用方法失败，则使用手动分步写入：
1. **第一步**：计算新PC值并写入到地址0
2. **第二步**：写入EPC数据到地址1
3. **错误处理**：每步都有详细的日志记录

### 3. 使用方法
当用户在标签管理界面选择EPC区域（Bank选择为EPC）进行写入时：
1. 系统会自动检测是否为EPC区域写入
2. 如果是EPC区域，会调用改进的写入方法
3. 自动处理PC字段和EPC数据的写入
4. 提供详细的日志输出便于调试

## 技术细节

### PC字段结构
- PC字段是16位（2字节）
- bit[15:11] 存储EPC长度信息（以字为单位）
- bit[10:0] 存储其他控制信息

### EPC区域结构
```
地址0: PC字段 (2字节)
地址1: EPC数据开始
地址N: CRC字段 (2字节，由标签芯片自动计算)
```

### 写入策略优化
1. **地址0或1写入**：
   - 优先使用 `writeEpcString()` 方法
   - 失败时使用手动分步写入
2. **其他地址写入**：使用原有逻辑

### 错误处理
- 每个写入步骤都有错误检查
- 详细的日志记录便于问题诊断
- 多种备用策略确保写入成功率

## 使用效果
- ✅ 支持写入任意长度的EPC数据
- ✅ 自动维护PC字段的一致性
- ✅ 提高RFID标签的兼容性
- ✅ 减少写入失败的情况
- ✅ 详细的日志输出便于调试

## 调试信息
修改后的代码会输出详细的日志信息：
```
writeEpcWithPcUpdate: originalEpc=..., startAddress=0, newEpcData=...
writeEpcWithPcUpdate: using writeEpcString with password=...
writeEpcWithPcUpdate: writeEpcString successful
```

或者在备用方案时：
```
writeEpcWithPcUpdate: writeEpcString failed with result=252
writeEpcWithPcUpdate: trying manual PC+EPC write
writeEpcManually: calculated newPc=1000
writeEpcManually: PC write successful
writeEpcManually: EPC write successful
```

## 注意事项
- 确保RFID标签支持相应长度的EPC数据
- 写入前确认访问密码正确
- 建议在写入前备份原始标签数据
- 查看日志输出以诊断写入问题