#! /bin/bash

if [ $# -lt 1 ]; then
	echo '命令行参数不足'
	exit 1
fi

filename=$1
if [ ! -r $filename ]; then
	echo '文件不存在或不可读: ' $filename
	exit 1
fi
echo '日志文件: ' $filename

reg='((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])'
grep -o -E $reg $filename | sort | uniq -c | sort -nr | head -10

'''
grep -o -E $reg $filename | sort | uniq > /tmp/logip1.txt

flag=0
for line in `cat /tmp/logip1.txt`; do
	ipmatch="^$line$"
	num=`cat /tmp/logip0.txt | grep $ipmatch | wc -l`
	if [ $flag -eq 0 ]; then
		flag=1
		echo $num' '$line > /tmp/logip2.txt
	else
		echo $num' '$line >> /tmp/logip2.txt
	fi
done

sort -rn /tmp/logip2.txt | head -10
'''