# Demo介绍
    本demo主要是为了跑通声网的屏幕分享功能，将客户关心的点和我们经常需要给客户演示的点在demo中体现出来
    方便同学们在集成屏幕共享的时候能够快速的对屏幕共享的功能进行演示。

# Demo快速跑通指南
    在 gradle.properties 中配置您的appid 和 appCer等信息即可

# 集成方式
    如果需要手动集成，只需将jar和aar放在agora-libs下面
    将so文件放在 agora-jniLibs下面，项目会自动检查这两个文件夹下是否存在库文件
    如果存在，那么会自动采用手动集成的方式进行集成，如果不存在会通过maven库加载
    （这里并不保证检查库文件的正确与否）

# Demo主要功能介绍
    # BASIC：
        1、加入声网RTC频道
        2、启动和停止屏幕分享
        3、支持选择不同的scenario启动屏幕共享
        4、支持 开/关 发送mic audio到远端
        5、支持 开/关 发送screen audio到远端
        6、支持 开/关 发送screen video到远端
        7、支持 开/关 预览屏幕分享
        8、支持调节 mic volume
        9、支持调节 screen volume
        10、支持查看当前SDK版本号
        11、支持点击Video放大/缩小
    # ADVANCED（多次点击版本号）
        1、支持 开/关 网络上下行状态信息
        2、支持 开/管 生成video dump文件
        3、支持调节屏幕分享分辨率/码率等信息
        4、支持设置音量增益倍数
        5、支持设置sdk log文件大小