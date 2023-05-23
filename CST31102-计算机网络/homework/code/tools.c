#include "tools.h"
#include <ctype.h>
#include <stdio.h>

// First is the calculation of the checksum
unsigned short checksum(unsigned short *src, int cnt) {
    register unsigned short *u_tmp = src;
    register long sum = 0;
    while (cnt > 1) {
        sum += *u_tmp++;
        cnt -= 2;
    }
    if (cnt > 0)
        sum += *(unsigned char *)u_tmp;
    while (sum >> 16) 
        sum = (sum & 0xffff) + (sum >> 16);
    return ~sum;
}

// 32-bit redundant round-robin checksum
unsigned int CRC32Bit(char *data, int cnt) {
    unsigned int STORE[256];
    for (int i = 0; i < 256; i++) {
        STORE[i] = i;
        for (int j = 0; j < 8; j++) 
            STORE[i] = (STORE[i] >> 1) ^ ((STORE[i] & 1) ? POLYNOMIAL : 0);
    }
    unsigned int num = 0;
    num = ~num;
    for (int i = 0; i < cnt; i++)
        num = (num >> 8) ^ STORE[(num ^ data[i]) & 0xff];
    return ~num;
}

// Convert ip address strings to addresses
unsigned int ipToaddress(const char *constAddr) {
    int init = 10, shiftTime = 0, cycles = 0;
    unsigned int address = 0, tmp = 0;
    char ch = *constAddr;
    while (1) {
        tmp = 0;
        while (1) {
            if (isdigit(ch)) {
                tmp = (tmp * init) + (ch - 0x30);
                ch = *++constAddr;
            } 
            else
                break;
        }
        shiftTime = 8 * cycles++;
        tmp <<= shiftTime;
        address += tmp;
        if (ch == '.') 
            ch = *++constAddr;
        else
            break;
    }
    return address;
}

void ipAdressToStr(unsigned int in, char Str[]) {
    unsigned char word[4];
    word[0] = (in >> 24) & 0xFF;
    word[1] = (in >> 16) & 0xFF;
    word[2] = (in >> 8) & 0xFF;
    word[3] = in & 0xFF;
    sprintf(Str, "%d.%d.%d.%d", word[3], word[2], word[1], word[0]);
}
