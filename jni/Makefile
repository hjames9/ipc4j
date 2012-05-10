CC=gcc
CFLAGS=-Wall -Werror -Wextra -g -fPIC -shared -m64 -O0
INCLUDES=-I${JAVA_HOME}/include
SOURCES=org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter.c \
 org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter.c \
 org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter.c \
 utils.c
LIB=libipc4j.so

all:
	$(CC) $(CFLAGS) $(INCLUDES) $(SOURCES) -o $(LIB)

clean:
	$(RM) $(LIB) *.o
