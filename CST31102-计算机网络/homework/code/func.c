#include "tools.h"
#include <arpa/inet.h>
#include <assert.h>
#include <ctype.h>
#include <malloc.h>
#include <net/if.h>   
#include <sys/ioctl.h>  
#include <sys/socket.h>
#include <sys/types.h>
#include <time.h>    
#include <unistd.h> 
#include <netinet/ether.h>     
#include <netpacket/packet.h>  
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

MACAdress localMAC;  // Note that you need to set its value to the MAC address of the local machine in order to run correctly!
MACAdress desMAC = {0x00, 0x15, 0x5d, 0xc0, 0x77, 0x8e};  // Destination MAC address
char localIpAddress[] = "172.21.91.157";  // Note that you need to set its value to the IP address of the local machine in order to run correctly! 
char desIp[] = "172.21.91.157";           // Note that you need to set its value to the IP address of the destination machine in order to run correctly! 

// Macro-define each constant using define
#define LEN_OF_IP_HEADER 5                    // Length of IP prefix
#define LEN_OF_UDP_HEADER 8                   // Length of UDP header
#define IP_HDR_LEN 20                         // Length of IP header 
#define LEN_OF_MAC_HEADER 14                  // Length of MAC frames
#define LEN_OF_MACFCS 4                       // Trailing length of MAC frames
#define MIN_LEN_OF_DATA 46                    // Minimal extent of the data section
#define MAX_LEN_OF_FRAME 1518                 // max frame length
#define PROTOCAL_TYPE_NUMBER 0x0800           // IPv4 protocol corresponding number
#define ERROR_TAKEN 0                    
#define PROTOCAL_USED_HERE 0x0800             // Customized protocol number
#define MTU 44                                // Maximum data unit length
#define MAX_IP_PACKET_LEN 65536               // Maximum packet length


typedef unsigned char uchar;    // using typedef to make code simple
typedef uchar MACAdress[6];
typedef uchar checksumNum[4];

const unsigned char UDP_PROTOCAL = (unsigned char)17U;  // Protocol number used for UDP

// Define a structure for the implementation of the IP data header
struct IpHeader {
    unsigned int sourceAdd;   
    unsigned int destiAdd;  
    unsigned char HeaderLength : 4;
    unsigned char version : 4;  // IP version
    unsigned char SerType;  
    unsigned short int totalLen; 
    unsigned short int id;        
    unsigned short int offset;  
    unsigned char TimeToLive;        
    unsigned char protocol;   
    unsigned short int checkNum;     
};

// Define a structure for the implementation of the UDP data header
struct UDP_Header {
    unsigned short int sourcePort;
    unsigned short int desPort;
    unsigned short int len;
    unsigned short int checkNum;
};






int socketRaw;
struct sockaddr_ll socketSend;           
struct sockaddr_ll socketReceive;         
unsigned int destinationIP, sourceIP;       



typedef unsigned char checksumNum[4]; 

// The following parameters are used for the slicing function in the ip protocol

int idxInFrame;                             
char store[MAX_IP_PACKET_LEN];              
int packetIndex = -1;                      
int bytesInTotal = LEN_OF_MACFCS + LEN_OF_MAC_HEADER;  
struct IpHeader *first_iphdr;               


unsigned short int checkSumInUDP(char *udp_datageam, uint udplen, uint sourceIP,uint destinationIP) {
    char tmp[MAX_IP_PACKET_LEN];
    unsigned short int protocal = 17;
    memset(tmp, 0, sizeof(tmp));
    memcpy(tmp, &sourceIP, 4);
    memcpy(tmp + 4, &destinationIP, 4);
    memcpy(tmp + 9, &protocal, 1);
    memcpy(tmp + 10, &udplen, 2);
    memcpy(tmp + 12, udp_datageam, udplen);
    unsigned short int num = checksum((short *)tmp, udplen + 12);
    return num;
}

unsigned short int makingFrame(MACAdress *dst, MACAdress *src, unsigned short int protocal, char *payload,int payload_len, char *frame) {
    if (payload_len > 1500)
        return 0; 
    memcpy(&frame[0], dst, 6);
    memcpy(&frame[6], src, 6);
    memcpy(&frame[12], &protocal, 2);
    memcpy(&frame[14], payload, payload_len);
    if (payload_len < MIN_LEN_OF_DATA) {
        char zeros[MIN_LEN_OF_DATA];
        bzero(zeros, sizeof(zeros));
        memcpy(&frame[LEN_OF_MAC_HEADER + payload_len], zeros,MIN_LEN_OF_DATA - payload_len);
    }
    int lengthOfData = payload_len < MIN_LEN_OF_DATA ? MIN_LEN_OF_DATA : payload_len;
    unsigned int  CRCNum = CRC32Bit(frame, LEN_OF_MAC_HEADER + lengthOfData);
    memcpy(&frame[LEN_OF_MAC_HEADER + lengthOfData], &CRCNum, sizeof(CRCNum));
    return LEN_OF_MAC_HEADER + LEN_OF_MACFCS + lengthOfData;
}


void output_frame(char *frame, int frame_size) {
    char *p = frame;
    int cnt = frame_size;
    int t = 0;
    while (cnt > 1) {
        printf("%hhx ", *p++);
            t++;
        if (t % 16 == 0) 
            printf("\n");
        cnt--;
    }
    printf("\n");
}

void fromNetToDatalink(char data[], int data_len, int error) {
    char frame[MAX_LEN_OF_FRAME];
    int size;
    size = makingFrame(&desMAC, &localMAC, htons(PROTOCAL_USED_HERE), data,data_len, &frame[0]);
    // Exceed MTU limit
    if (!size) {
        printf("Data length can't be more than 1500 bytes!\n");
        return;
    }
    if (error == 1)
        frame[14] = ~frame[14];
    else if (error == 2)
        frame[21] = ~frame[21];
    else if (error == 3)
        frame[36] = ~frame[36];
    if (send(socketRaw, frame, size, 0) == -1) {
        perror("Send Error");
    } else {
        printf("Successfully send %d bytes data\n\n", size);
    }
}

// From the transport layer to the network layer
void fromTransToNet(uchar protocal, uint payload_len, uchar *payload) {
    struct UDP_Header u;
    struct IpHeader iphdr;
    char ip_packet[MAX_LEN_OF_FRAME * 2];
        memcpy(&u, payload, 8);
    bzero(ip_packet, sizeof(ip_packet));
    // This code implements ipv4
    iphdr.version = 4;  
    iphdr.HeaderLength = LEN_OF_IP_HEADER;
    iphdr.SerType = 0;
    iphdr.totalLen = htons(payload_len + (iphdr.HeaderLength << 2));  // Fixed length of 20 bytes
    iphdr.id = rand() % 10000;
    iphdr.offset = 0;
    iphdr.TimeToLive = 128;
    iphdr.protocol = protocal;
    iphdr.destiAdd = destinationIP;
    iphdr.sourceAdd = sourceIP;
    iphdr.checkNum = 0;
    struct IpHeader test;
    uchar more_frag;
    if (payload_len + (iphdr.HeaderLength << 2) > MTU) {
        int nfrag = payload_len / (MTU - IP_HDR_LEN);
        int last_frag_len = payload_len % (MTU - IP_HDR_LEN);
        nfrag = last_frag_len ? nfrag + 1 : nfrag;
        int each_size = MTU - IP_HDR_LEN;
        int left = payload_len;
        for (int i = 0; i < nfrag; i++) {
            iphdr.totalLen = 0;
            left -= each_size;
            more_frag = left > 0;
            iphdr.totalLen = htons(more_frag ? MTU : last_frag_len + IP_HDR_LEN);
            iphdr.offset = i * each_size / 8;
            // set tag 1
            iphdr.offset = iphdr.offset | 0x2000;

            if (more_frag) {
                memcpy(ip_packet + sizeof(iphdr), payload + i * each_size,
                       each_size);

            } else {
                iphdr.offset = iphdr.offset & 0xDFFF;  // more frag = 0
                memcpy(ip_packet + sizeof(iphdr), payload + i * each_size,
                       each_size + left);
            }
            iphdr.checkNum = 0;
            iphdr.offset = htons(iphdr.offset);
            iphdr.checkNum = checksum((unsigned short int *)&iphdr, sizeof(iphdr));
            memcpy(ip_packet, &iphdr, sizeof(iphdr));
            fromNetToDatalink(ip_packet, ntohs(iphdr.totalLen),ERROR_TAKEN);
        }
    } else {
        iphdr.checkNum = checksum((unsigned short int *)&iphdr, sizeof(iphdr));

        memcpy(ip_packet, &iphdr, sizeof(iphdr));
        memcpy(ip_packet + sizeof(iphdr), payload, payload_len);
        fromNetToDatalink(ip_packet, ntohs(iphdr.totalLen), ERROR_TAKEN);
    }
}

void MakeUDPHeader(unsigned short int sourcePort, unsigned short int desPort, unsigned short int payload_len,struct UDP_Header *udphdr) {
    udphdr->sourcePort = htons(sourcePort);
    udphdr->desPort = htons(desPort);
    udphdr->len = htons(payload_len + LEN_OF_UDP_HEADER);
    udphdr->checkNum = 0;
}


void fromAppToTrans(unsigned short int sourcePort, unsigned short int desPort, char *payload,uint payload_len) {
    struct UDP_Header UDPheader;
    char udp_datagram[MAX_IP_PACKET_LEN];
    MakeUDPHeader(sourcePort, desPort, payload_len, &UDPheader);
    memcpy(udp_datagram, &UDPheader, sizeof(UDPheader));
    memcpy(udp_datagram + sizeof(UDPheader), payload, payload_len);
    UDPheader.checkNum = checkSumInUDP(udp_datagram, payload_len + sizeof(UDPheader), sourceIP, destinationIP);
    memcpy(udp_datagram, &UDPheader, sizeof(UDPheader));
    fromTransToNet(UDP_PROTOCAL, payload_len + sizeof(UDPheader), udp_datagram);
}


void dataSendInUse(uint sourcePort, uint desPort, char *data) {
    fromAppToTrans(sourcePort, desPort, data, strlen(data));
}


int CompareMAC(MACAdress x, MACAdress y) {
    for (int i = 0; i < 6; i++)
        if (x[i] != y[i]) return 0;
    return 1;
}


int checkSumInIP(struct IpHeader *p, int len) {
    return checksum((unsigned short int *)p, len) != 0;
}


void fromNetToTrans(int bytesNum, char *packet, struct IpHeader *headerIp,int idxInFrame) {
    char udp_datagram[MAX_IP_PACKET_LEN];
    struct UDP_Header inudphdr;
    char data[MAX_IP_PACKET_LEN];
    unsigned short int leftlen = bytesNum - LEN_OF_MAC_HEADER - LEN_OF_MACFCS - IP_HDR_LEN;
    memcpy(udp_datagram, packet + IP_HDR_LEN, leftlen);
    udp_datagram[leftlen] = '\0';
    memcpy(&inudphdr, &udp_datagram, 8);
    unsigned short int udphdr_eror = checkSumInUDP(udp_datagram, ntohs(inudphdr.len),headerIp->sourceAdd, headerIp->destiAdd);
    if (udphdr_eror) {
        printf(
            "[ERROR]: frame[%d] takes error caused by failed UDP "
            "checksum.\n",
            idxInFrame);
        return;
    }
    printf("[UDP Info]    source port %u | dest port %u\n", ntohs(inudphdr.sourcePort),ntohs(inudphdr.desPort));
    strcpy(data, &udp_datagram[LEN_OF_UDP_HEADER]);
    printf("[content]   %s\n", data);
}



void threadSend() {
    printf("Please wait while initializing the sending thread......\n");
    while (1) {
        char info[MAX_IP_PACKET_LEN];
        fgets(info, MAX_IP_PACKET_LEN, stdin);
        dataSendInUse(5000, 8080, info);
    }
}


int FrameCheckByCRC(uint CAS, char inputBuffer[], int bytesNum) {
    char CRC_STORE[4];  
    memcpy(&CRC_STORE, &CAS, sizeof(CAS));
    if (CRC_STORE[0] != inputBuffer[bytesNum - 4] || CRC_STORE[1] != inputBuffer[bytesNum - 3] || CRC_STORE[2] != inputBuffer[bytesNum - 2] || CRC_STORE[3] != inputBuffer[bytesNum - 1]) 
    {
        printf("Error happended in sending frame[%d]!\n", idxInFrame);
        return -1;
    }
    return 0;
}

// From the data link layer to the network layer
int fromDatalinkToNet(char packet[], int bytesNum) {
    struct IpHeader headerIp;
    unsigned int inipaddr;
    memcpy(&headerIp, packet, IP_HDR_LEN);

    char desIp[16], sourceIp[16];
    ipAdressToStr(headerIp.destiAdd, desIp);
    ipAdressToStr(headerIp.sourceAdd, sourceIp);
    if (checkSumInIP(&headerIp, sizeof(headerIp))) {
        printf("Error happaned in sending frame[%d]\n",idxInFrame);
        return -1;
    }
    headerIp.totalLen = ntohs(headerIp.totalLen);

    // Output of the corresponding information at the ip layer
    printf(
        "[IP Info]    from %s to %s |  HeaderLength %2u | Version: IPv%u| Type: %u | Total "
        "Length: %4u | id %6u | "
        "TimeToLive: %4u | protocal: %2u | checksum: %x\n",
        sourceIp, desIp,  headerIp.HeaderLength, headerIp.version, headerIp.SerType,(headerIp.totalLen), headerIp.id, headerIp.TimeToLive, headerIp.protocol,headerIp.checkNum);
    headerIp.offset = ntohs(headerIp.offset);
    if ((headerIp.offset & 0x2000) || headerIp.offset) {
        int lastFlag = !((headerIp.offset & 0x2000) >> 13);
        ushort addroff = 8 * (headerIp.offset & 0x1FFF);
        int firstFlag = !lastFlag && !addroff;
        if (firstFlag) {
            printf("\nThis is the first fragment");
            printf("\n****************************\n");
            packetIndex = headerIp.id;
            memcpy(store, packet, headerIp.totalLen);
            first_iphdr = &headerIp;
            bytesInTotal += headerIp.totalLen;
            struct UDP_Header u;
            memcpy(&u, packet + IP_HDR_LEN, 8);
        } 
        else if (lastFlag) {
            memcpy(store + IP_HDR_LEN + addroff, packet + IP_HDR_LEN,headerIp.totalLen - IP_HDR_LEN);
            bytesInTotal += headerIp.totalLen - IP_HDR_LEN;
            fromNetToTrans(bytesInTotal, store, first_iphdr,idxInFrame);
            packetIndex = -1;
            first_iphdr = NULL;
            bzero(store, sizeof(store));
            bytesInTotal = LEN_OF_MACFCS + LEN_OF_MAC_HEADER;
            printf("\nThis is the last fragment\n");
            printf("****************************\n");
        } 
        else if (packetIndex == headerIp.id) {
            memcpy(store + IP_HDR_LEN + addroff, packet + IP_HDR_LEN,headerIp.totalLen - IP_HDR_LEN);
            bytesInTotal += headerIp.totalLen - IP_HDR_LEN;
        }
    } 
    else 
        fromNetToTrans(bytesNum, packet, &headerIp, idxInFrame);
}


void receiveData() {
    int bytesNum;
    char inputBuffer[1536];  
    MACAdress srcMAC;   // Sender's MAC address
    MACAdress desMAC;   // Destination Address
    idxInFrame = 0;
    while (1) {
        inputBuffer[0] = '\0';
        socklen_t addr_length = sizeof(struct sockaddr_ll);
        bytesNum = recvfrom(socketRaw, inputBuffer, 1536, 0,(struct sockaddr *)&socketReceive,&addr_length);  
        if (bytesNum < LEN_OF_MAC_HEADER + LEN_OF_MACFCS) 
            continue;
        unsigned short int protocal; 
        memcpy(&desMAC, &inputBuffer, 6);       
        memcpy(&srcMAC, &inputBuffer[6], 6);    
        memcpy(&protocal, &inputBuffer[12], 2);  
        if (!CompareMAC(desMAC, localMAC)) 
            continue;  
        if (protocal != ntohs(PROTOCAL_USED_HERE)) 
            continue;
        idxInFrame++; 
        printf("\n");
        if (FrameCheckByCRC(CRC32Bit(inputBuffer, bytesNum - 4), inputBuffer, bytesNum) == -1) 
            continue;
        printf("[MAC Info]    source: MAC %02X:%02X:%02X:%02X:%02X:%02x | MAC Protocal: %x | receive %d bytes in total\n",srcMAC[0], srcMAC[1], srcMAC[2], srcMAC[3], srcMAC[4],srcMAC[5], ntohs(protocal), bytesNum);
        char packet[MAX_LEN_OF_FRAME];
        memcpy(packet, &inputBuffer[LEN_OF_MAC_HEADER],bytesNum - LEN_OF_MACFCS - LEN_OF_MAC_HEADER);
        fromDatalinkToNet(packet, bytesNum);
    }
}

void threadReceive() {
    printf("Please wait while initializing the receiving thread......\n");
    receiveData();
}


int main() {
    srand(time(NULL));
    socketRaw = socket(PF_PACKET, SOCK_RAW, htons(ETH_P_ALL));
    struct ifreq t; 
    strncpy(t.ifr_name, "eth0", IFNAMSIZ); 
    if (ioctl(socketRaw, SIOCGIFHWADDR, &t) == 0) 
        memcpy(localMAC, &t.ifr_addr.sa_data, sizeof(localMAC));
    printf("MAC: %02X:%02X:%02X:%02X:%02X:%02x\n", localMAC[0], localMAC[1], localMAC[2], localMAC[3], localMAC[4],localMAC[5]);
    if (ioctl(socketRaw, SIOCGIFINDEX, &t) == -1) {
        close(socketRaw);
        return -1;
    }
    bzero(&socketSend, sizeof(socketSend));
    socketSend.sll_family   =  AF_PACKET;
    socketSend.sll_halen    =  ETH_ALEN;
    socketSend.sll_ifindex  =  t.ifr_ifindex;  
    socketSend.sll_protocol =  htons(ETH_P_ALL);  
    if (bind(socketRaw, (struct sockaddr *)&socketSend, sizeof(socketSend)) ==-1) {
        perror("Binding failed！\n");
        return 1;
    }
    
    destinationIP = ipToaddress(desIp);
    sourceIP = ipToaddress(localIpAddress);
    pthread_t pSend, pReiceive;
    if (pthread_create(&pSend, NULL, (void *)threadSend, NULL) != 0) {
        printf("Failed to create a send thread!\n");
        return 1;
    }
    if (pthread_create(&pReiceive, NULL, (void *)threadReceive, NULL) != 0) {
        printf("Failed to create a receiving thread!\n");
        return 1;
    }
    pthread_join(pSend, NULL);
    pthread_join(pReiceive, NULL);
    return 0;
}
