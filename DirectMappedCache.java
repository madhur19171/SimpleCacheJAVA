package COAssignment;

import java.util.*;

public class DirectMappedCache {

    static class MainMemory {
        int[][] block;
        int number_block;

        MainMemory(int number_block) {
            this.number_block = number_block;
            block = new int[1024][number_block];
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

    static class DirectMapped extends CacheLine {
        CacheLine[] cache;
        int number_block;
        MainMemory MM;

        DirectMapped(int number_block, int tag, int cache_lines) {
            super(number_block, tag);
            this.number_block = number_block;
            cache = new CacheLine[cache_lines];
            for (int i = 0; i < cache_lines; i++)
                cache[i] = new CacheLine(number_block, tag);
            MM = new MainMemory(number_block);
        }

        int[] breakAddress(String address) {
            int olength = (int) (Math.log(number_block) / Math.log(2));  //3
            int ilength = (int) Math.ceil(Math.log(cache.length) / Math.log(2));  //4
            int tlength = address.length() - olength - ilength; // 25
            int offset = Integer.parseInt(address.substring(address.length() - olength), 2);
            int index = Integer.parseInt(address.substring(tlength, address.length() - olength), 2);
            int tag = Integer.parseInt(address.substring(0, tlength), 2);
            return new int[]{tag, index, offset};
        }

        int read(String address) {
            int[] breakAddress = breakAddress(address);
            int tag = breakAddress[0];
            int index = breakAddress[1];
            int offset = breakAddress[2];
            CacheLine line = cache[index];
            if (line.tag != tag) {
                System.out.println("Read Miss");
                //Bring Data From Main Memory
                //Write It To Cache
                line.tag = tag;
                line.block = MM.read(address);
                //Read It.
            }
            return line.read(offset);
        }

        void write(int data, String address) {
            int[] breakAddress = breakAddress(address);
            int tag = breakAddress[0];
            int index = breakAddress[1];
            int offset = breakAddress[2];
            //System.out.println(Arrays.toString(breakAddress));
            CacheLine line = cache[index];
            if (line.tag != tag) {
                System.out.println("Write Miss");
                //Bring Data From Main Memory
                //Write It To Cache
                line.tag = tag;
                line.block = MM.read(address);
                //Overwrite data onto it.
            }
            line.write(data, offset);
            //Write Through
            MM.write(line.block, address);
        }

        void print() {
            System.out.println("Line\tTag\t\tData");
            int ind = 0;
            for (CacheLine cl : cache)
                System.out.println(ind++ + "\t\t" + cl.tag + "\t\t" + Arrays.toString(cl.block));
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
        DirectMapped DM = new DirectMapped(B, 0, CL);
        System.out.println("Enter Queries");
        outer:
        while (true) {
            String str = kb.next();
            switch (str.toUpperCase().charAt(0)) {
                case 'E':
                    break outer;
                case 'R':
                    DM.read(kb.nextLine());
                    break;
                case 'W':
                    String addr = kb.next();
                    DM.write(kb.nextInt(), addr);
                    break;
                case 'T':
                    String source = kb.next();
                    String destination = kb.next();
                    int data = DM.read(source);
                    DM.write(data, destination);
                    break;
                case 'P':
                    DM.print();
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