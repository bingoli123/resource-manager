# resource-manager Changelog

## [3.1.0]

### Fixed

- 修复校验纳管平台下的分组时可以纳管重名监控点的问题
- 修改监控点同步经纬度等信息的逻辑
- 优化资源树查询逻辑
- 更新监听kong redis mysql rabbitmq等组件的变化
- 处理查询框输入% _ \等特殊字符的转义

## [3.1.0_rc.14]

### Fixed

- 删除服务启动是绑定mq exchange和queue

## [3.1.0_rc.14]

### Fixed

- 修复校验纳管平台下的分组时可以纳管重名监控点的问题

## [3.1.0_rc.13]

### Fixed

- 修改更新分组下的监控点处理缓慢的问题，改为直接使用sql执行更新逻辑

## [3.1.0_rc.12]

### Fixed

- 修复分非强制更新监控点会将所有监控点同步成相同名称的问题

## [3.1.0_rc.11]

### Fixed

- 修复分非强制更新监控点会将所有监控点同步成相同名称的问题

## [3.1.0_rc.10]

### Fixed

- 修改监控点同步经纬度等信息的逻辑

## [3.1.0_rc.9]

### Fixed

- 优化资源树查询逻辑

## [3.1.0_rc.8]

### Fixed

- 优化资源树查询逻辑
- 优化查询节点下的监控点接口查询逻辑

## [3.1.0_rc.7]

### Fixed

- 监控点同步接口未同步地址信息

## [3.1.0_rc.6]

### Fixed

- 解决离线原因修改不成null的问题
- 解决资源树搜索未检索分组的问题

## [3.1.0_rc.5]

### Fixed

- 纳管设备的时候校验同一分组下的监控点不能重名
- 纳管时离线原因不能存储到监控点的表中

## [3.1.0_rc.4]

### Fixed

- 更新监听kong redis mysql rabbitmq等组件的变化

## [3.1.0_rc.3]

### Fixed

- 修改查询资源管理树节点的排序方式
- 修改监控点修改接口无法修改经纬度为空的问题

## [3.1.0_rc.2]

### Fixed

- 处理查询框输入% _ \等特殊字符的转义
- 修改平台纳管过滤已删除的设备
- 修改查询角色资源树排序方式

## [3.1.0_rc.1]

### Fixed

- 解决递归查询资源树时查询到其他目录的资源的bug

## [3.1.0_rc.0]

### Fixed

- rc版本发布

## [3.1.0_beta.5]

### Fixed

- 修改liquibase对接逻辑

## [3.1.0_beta.4]

### Fixed

- 修改packfile
- 修改liquibase对接逻辑

## [3.1.0_beta.3]

### Fixed

- 修改chart中绑定节点

## [3.1.0_beta.2]

### Fixed

## [3.0.0_rc.1]

### Fixed

- 平台脱管目录校验查询到的设备为空

## [3.0.0_rc.0]-2022-01-18

### Fixed

- 修改端口号到30160

- 设备媒体同步返回body值

## [3.0.0_beta.19]-2022-01-14

### Fixed

- 修改日志配置

## [3.0.0_beta.18]-2022-01-14

### Fixed

- 修改日志配置

## [3.0.0_beta.17]-2022-01-13

### Fixed

- 平台目录纳管如果纳管的目录和资源树下的目录重名则提示用户
- 删除虚拟资源树时候校验资源树是否已经关联角色，如果关联角色提示用户
- 动态从consul获取logback日志配置
- 更新dockerfile基础镜像
- 生成logrotate配置文件

## [3.0.0_beta.16]-2022-01-12

### Fixed

- 资源树接口返回http状态码修改
- 判断不能禁用或删除基本资源树
- 数据库连接池增加最小连接数配置

## [3.0.0_beta.15]-2022-01-05

### Fixed

- 修改application.yml生成逻辑
- 添加dockerfile和helmcharts相关文件
- 校验名称重复相关接口返回值由value修改为Available

## [3.0.0_beta.14]-2021-12-31

### Fixed

- fix : 修改数据中 类似是否的字段修改为 布尔类型
- 资源管理查询监控点回查询到所有的监控点
- 增加事件和监控点绑定关系mq监听

## [3.0.0_beta.13]-2021-12-29

### Fixed

- 数据字典修改成string之后 数字使用==判断相等的问题修改
- 和其他服务统一配置文件格式，动态生成bootstrap配置文件

## [3.0.0_beta.12]-2021-12-27

### Fixed

- 查询监控点树增加返回监控点绑定的事件
- 纳管的时候增加描述字段同步
- mq监控点事件绑定消息消费逻辑功能
- 修改数据字典

## [3.0.0_beta.11]-2021-12-16

### Fixed

- 监控点搜索增加在线状态查询条件
- 修复分层查询节点下没有监控点报错问题

## [3.0.0_beta.10]-2021-12-15

### Fixed

- 分层查询监控点和监控点搜索接口增加返回监控点在线状态字段

## [3.0.0_beta.9]-2021-12-08

### Fixed

- 媒体纳管时修改获取的媒体的在线状态

## [3.0.0_beta.8]-2021-12-07

### Fixed

- 纳管时经纬度设置修改
- 更新设备时经纬度未修改的设备不覆盖监控点经纬度
- 修改虚拟资源树名称时同时修改资源树的根目录名称，修改根目录名称时同时修改资源树名称

## [3.0.0_beta.7]-2021-12-06

## Added

- 资源树搜索接口增加返回路径名称字段
- 项目文件结构重构

## [3.0.0_beta.6]-2021-12-02

### Fixed

- 在设备接入页面批量纳管，选择导入分组，纳管成功后 ，在资源管理页面点击对应得分组，报错
- 对已纳管得设备进行再次批量纳管，选择不与之前不同得分组，并且选择导入设备资源作为分组，纳管成功后，资源管理页面并未生成相应的分组，纳管得根分组没有发生变化
- 人像视图-属性检索--选择监控点对话框中搜索框输入%或_可以检索出全部内容

## Added

- 项目文件结构重构

## [3.0.0_beta.5]

### Fixed

- 修复项目启动时未生成consul配置的问题

## [3.0.0_beta.4]

### Fixed

- 批量对设备进行纳管，勾选导入分组，纳管报系统忙，当选中全部纳管数据和部分纳管数据是选中纳管分组会报空指针问题
- consul统一配置修改
- 更新项目模块的groupId为com.inspur.ivideo
- 更新异常提示信息
- 修改资源管理同连接管理和权限管理实体类相互引用的问题

## [3.0.0_beta3]