package COAssignment;

import java.util.*;

public class FullyAssociativeCache {

    static class MainMemory {
        int[][] block;
        int number_block;

        MainMemory(int number_block) {
            this.number_block = number_block;
            block = new int[100000][number_block];
        }

        int[] read(String address) {
            int x = (int) Math.ceil(Math.log(number_block) / Math.log(2));
            x = Integer.parseInt(address.substring(0, address.length() - x), 2);
            return block[x];
        }

        void write(int[] data, String address) {
            int x = (int) Math.ceil(Math.log(number_block) / Math.log(2));
            x = Integer.parseInt(address.substring(0, address.length() - x), 2);
            block[x] = data;
        }
    }

    static class CacheLine {
        int[] block;
        int tag;

        CacheLine(int number_block, int tag) {
            this.tag = tag;
            block = new int[number_block];
        }

        void write(int data, int block_number) {
            block[block_number] = data;
        }

        int read(int block_number) {
            return block[block_number];
        }
    }

    static class FullyAssociative extends CacheLine {
        CacheLine[] cache;
        int number_block;
        MainMemory MM;
        ArrayList<Integer> queue; //LRU Queue
        int hit;
        int miss;

        FullyAssociative(int number_block, int tag, int cache_lines) {
            super(number_block, tag);
            this.number_block = number_block;
            cache = new CacheLine[cache_lines];
            queue = new ArrayList<>(0);
            for (int i = 0; i < cache_lines; i++) {
                cache[i] = new CacheLine(number_block, tag);
                queue.add(i);
            }
            MM = new MainMemory(number_block);
            hit = 0;
            miss = 0;
        }

        int[] breakAddress(String address) {
            int olength = (int) (Math.log(number_block) / Math.log(2));  //3
            int tlength = address.length() - olength; // 29
            int offset = Integer.parseInt(address.substring(tlength), 2);
            int tag = Integer.parseInt(address.substring(0, tlength), 2);
            return new int[]{tag, offset};
        }

        int read(String address) {
            int[] breakAddress = breakAddress(address);
            int tag = breakAddress[0];
            int offset = breakAddress[1];
            int index = -1;
            //Check If Required CacheLine Is Present In Current Cache
            for (int i = 0; i < cache.length; i++)
                if (cache[i].tag == tag)
                    index = i;
            if (index == -1) {
                System.out.print("Read Miss ");
                miss++;
                index = queue.get(0);
                queue.remove(0);
                queue.add(index);
                //Bring Data From Main Memory
                //Write It To Cache
                cache[index].tag = tag;
                cache[index].block = MM.read(address);
                //Read It.
            } else {
                queue.remove((Integer) index);
                queue.add(index);
                System.out.print("Read Hit ");
                hit++;
            }
            return cache[index].read(offset);
        }

        void write(int data, String address) {
            int[] breakAddress = breakAddress(address);
            int tag = breakAddress[0];
            int offset = breakAddress[1];
            int index = -1;
            //Check If Required CacheLine Is Present In Current Cache
            for (int i = 0; i < cache.length; i++)
                if (cache[i].tag == tag)
                    index = i;
            if (index == -1) {//Line Not Present In Cache
                System.out.println("Write Miss");
                miss++;
                index = queue.get(0);
                //Updating LRU Queue
                queue.remove(0);
                queue.add(index);
                //Bring Data From Main Memory
                //Write It To Cache
                cache[index].tag = tag;
                cache[index].block = MM.read(address);
                //Read It.
            } else {
                //Updating LRU Queue
                queue.remove((Integer) index);
                queue.add(index);
                System.out.println("Write Hit");
                hit++;
            }
            cache[index].write(data, offset);
            //Write Through
            MM.write(cache[index].block, address);
        }

        void print() {
            System.out.println("Line\tTag\t\tData");
            int ind = 0;
            for (CacheLine cl : cache)
                System.out.println(ind++ + "\t\t" + cl.tag + "\t\t" + Arrays.toString(cl.block));
            System.out.println("Hits - " + hit + "\tMiss - " + miss + "\tHit Ratio - " + (1.0 * hit / (hit + miss)));
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);
        System.out.println("Enter The Number of Cache Lines");
        int CL = kb.nextInt();
        System.out.println("Enter The Block Size");
        int B = kb.nextInt();
        kb.nextLine();
        FullyAssociative FA = new FullyAssociative(B, -1, CL);
        System.out.println("Enter Queries");
        outer:
        while (true) {
            String str = kb.nextLine();
            switch (str.toUpperCase().charAt(0)) {
                case 'E':
                    break outer;
                case 'R':
                    System.out.println(FA.read(str.substring(str.indexOf(' ') + 1)));
                    break;
                case 'W':
                    String addr = str.substring(str.indexOf(' ') + 1, str.lastIndexOf(' '));
                    int data = Integer.parseInt(str.substring(str.lastIndexOf(' ') + 1));
                    FA.write(data, addr);
                    break;
                case 'T':
                    String source = str.substring(str.indexOf(' ') + 1, str.lastIndexOf(' '));
                    String destination = str.substring(str.lastIndexOf(' ') + 1);
                    data = FA.read(source);
                    FA.write(data, destination);
                    break;
                case 'P':
                    FA.print();
            }
        }
    }
}
/*
        0000000000000000000000100 1010 001
        W 00000000000000000000001001010001 45
        R 00000000000000000000001001010001
        W 00000000000000000000001001010001 44
        R 00000000000000000000001001010001
 */

/*
write 00000000000000001010101111111111 2000
read 00000000000000001010101111111111
write 00000000000000000000000000000001 1234
read 00000000000000000000000000000001
write 00000000000000000000000000000101 1122
read 00000000000000000000000000000101
write 00000000000000001010101111111011 500
read 00000000000000001010101111111011
read 00000000000000001010101111111111
write 00000000000000000001001000101000 222
write 00000000000000000001001000101100 1232
read 00000000000000000001001000101100
write 00000000000000000001001000110011 999
write 00000000000000000001001000110111 1000
read 00000000000000000001001000110111
read 00000000000000000001001000110011
write 00000000000000001001100110011001 300
write 00000000000000001001100110011101 1200
read 00000000000000001001100110011101
read 00000000000000001001100110011001
write 00000000000000000000000000010000 2323
read 00000000000000000000000000010000
write 00000000000000000000000000010100 1212
read 00000000000000000000000000010100
write 00000000000000000011001100001000 32
read 00000000000000000011001100001000
write 00000000000000000011001100100111 123
read 00000000000000000011001100100111
write 00000000000000001111111111111111 5667
read 00000000000000001010101111111111
read 00000000000000001111111111111111
 */
