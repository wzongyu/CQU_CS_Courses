# 一、WSL环境配置  
代码运行在wsl环境下（Ubuntu）18.04 LTS。  
以管理员身份启动cmd命令行，然后输入wsl --install  
安装完成重启后，在Windows商店下载Ubuntu 18.04 LTS  
然后完成初始化设置进入Ubuntu  
sudo apt install gcc  
sudo apt update
运行代码的时候如果出现包不全的提示，按照建议的命令行下载即可。  

# 二、代码运行  
整个项目一共有四个文件，func.cpp, tools.h, tools.cpp, Makefile。  
需要修改func.cpp中定义的dst_mac数组为目的MAC地址，同时需要将ip修改为对应值。  
修改完成之后，同时启动两个Ubuntu终端，均切换到代码文件夹所在的目录，然后分别输入runtest命令即可测试功能。测试的是两个终端之间能否成功互相发消息，并且在传递的消息中体现了封装、解封、分片等功能。

