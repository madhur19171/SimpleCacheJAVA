package COAssignment;

import java.util.*;

public class SetAssociativeCache {

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

    static class SetAssociative extends FullyAssociative {
        int number_sets;
        int length_sets;
        FullyAssociative[] sets;

        SetAssociative(int num_sets, int len_sets, int number_block, int tag) {
            super(number_block, tag, len_sets);
            this.number_sets = num_sets;
            this.length_sets = len_sets;
            sets = new FullyAssociative[num_sets];
            for (int i = 0; i < sets.length; i++)
                sets[i] = new FullyAssociative(number_block, tag, len_sets);
        }

        int[] breakAddress(String address) {
            int olength = (int) (Math.log(number_block) / Math.log(2));  //3
            int ilength = (int) Math.ceil(Math.log(length_sets) / Math.log(2));  //4
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
            return sets[index].readFA(address, tag, offset);
        }

        void write(int data, String address) {
            int[] breakAddress = breakAddress(address);
            int tag = breakAddress[0];
            int index = breakAddress[1];
            int offset = breakAddress[2];
            sets[index].writeFA(address, data, tag, offset);
        }

        void print() {
            System.out.println("Set\t\tLine\tTag\t\tData");
            for (int i = 0; i < number_sets; i++) {
                System.out.print(i);
                sets[i].printFA();
            }
        }
    }

    static class FullyAssociative extends CacheLine {
        CacheLine[] cache;
        int number_block;
        MainMemory MM;
        ArrayList<Integer> queue; //LRU Queue

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

        }

        int readFA(String address, int tag, int offset) {
            int index = -1;
            //Check If Required CacheLine Is Present In Current Cache
            for (int i = 0; i < cache.length; i++)
                if (cache[i].tag == tag)
                    index = i;
            if (index == -1) {
                System.out.println("Read Miss");
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
            }
            return cache[index].read(offset);
        }

        void writeFA(String address, int data, int tag, int offset) {
            int index = -1;
            //Check If Required CacheLine Is Present In Current Cache
            for (int i = 0; i < cache.length; i++)
                if (cache[i].tag == tag)
                    index = i;
            if (index == -1) {//Line Not Present In Cache
                System.out.println("Write Miss");
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
            }
            cache[index].write(data, offset);
            //Write Through
            MM.write(cache[index].block, address);
        }

        void printFA() {
            int ind = 0;
            for (CacheLine cl : cache)
                System.out.println("\t\t" + ind++ + "\t\t" + cl.tag + "\t\t" + Arrays.toString(cl.block));
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);
        System.out.println("Enter The Number of Cache Lines");
        int CL = kb.nextInt();
        System.out.println("Enter Associativity");
        int AS = kb.nextInt();
        System.out.println("Enter The Block Size");
        int B = kb.nextInt();
        kb.nextLine();
        SetAssociative SA = new SetAssociative(CL / AS, AS, B, 0);
        System.out.println("Enter Queries");
        outer:
        while (true) {
            String str = kb.next();
            switch (str.toUpperCase().charAt(0)) {
                case 'E':
                    break outer;
                case 'R':
                    SA.read(kb.nextLine());
                    break;
                case 'W':
                    String addr = kb.next();
                    SA.write(kb.nextInt(), addr);
                    break;
                case 'T':
                    String source = kb.next();
                    String destination = kb.next();
                    int data = SA.read(source);
                    SA.write(data, destination);
                    break;
                case 'P':
                    SA.print();
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