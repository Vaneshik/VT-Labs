obj-m += vtfs.o
vtfs-objs := source/vtfs.o source/vtfs_ram_backend.o source/vtfs_http_backend.o source/http.o

PWD := $(CURDIR) 
KDIR = /lib/modules/`uname -r`/build
EXTRA_CFLAGS = -Wall -g

all:
	make -C $(KDIR) M=$(PWD) modules 

clean:
	make -C $(KDIR) M=$(PWD) clean
	rm -rf .cache
