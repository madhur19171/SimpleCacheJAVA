package COAssignment;

import java.util.*;

public class DirectMappedCache {
    /**
     * Creating a main memory of size 100000*number_of_blocks*4 bytes.
     * this memory is word indexed, that is each address can supply a unique word.
     * The Memory sends out data in blocks of word. The number of words in each block
     * are determined by the variable number_block.
     * This function provides two functions viz. Read and Write.
     * Read returns the entire block from which the appropriate hardware
     * that requested the data can read the appropriate word from the returned block.
     * Write function Takes the entire data block as the data to be written and the address
     * where the content has to be written.
     */
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

    /**
     * The CacheLine class is the heart of all the caches.
     * The CacheLine is implemented just like an actual cache
     * would work on a hardware level.
     * It consists of a tag and a data block as its attributes/
     * The tag uniquely Identifies the CacheLine and data stores the data/
     * It has two functions viz. Read and Write.
     * The Read function takes the block number to be read as the input.
     * It returns the data in that location of the block as the output.
     * The Write function takes the data as its input to be written and the
     * block number to where it is to be written. It returns nothing.
     */
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

    /**
     * The DirectMapped class mainly brings together different pieces.
     * It has an array of CacheLines just like a real cache.
     * Each cache line can be addressed by a unique address to it.
     * This class initiates a Main Memory and a CacheLine Array.
     * It also has two variables viz hit and miss to count the number of his and misses.
     */
    static class DirectMapped extends CacheLine {
        CacheLine[] cache;
        int number_block;
        MainMemory MM;
        int hit;
        int miss;

        DirectMapped(int number_block, int tag, int cache_lines) {
            super(number_block, tag);
            this.number_block = number_block;
            cache = new CacheLine[cache_lines];
            for (int i = 0; i < cache_lines; i++)
                cache[i] = new CacheLine(number_block, tag);
            MM = new MainMemory(number_block);
            hit = 0;
            miss = 0;
        }

        /**
         * This method is used to break the address into its appropriate components
         * and return those components.
         * The address is broken int 3 parts:
         * Offset: This determines the position of the required word inside the block.
         * Index: It is to index the appropriate CacheLine. This is key to the Direct Mapped Cache.
         * Tag: It is the part of the address which is compared with the Tag of the CacheLine to determine
         * if the cache line is the one that is required or not.
         *
         * @param address This  parameter  is the address which has to be broken
         * @return int[]
         */
        int[] breakAddress(String address) {
            int olength = (int) (Math.log(number_block) / Math.log(2));  //3
            int ilength = (int) Math.ceil(Math.log(cache.length) / Math.log(2));  //6
            int tlength = address.length() - olength - ilength; // 23
            int offset = Integer.parseInt(address.substring(address.length() - olength), 2);
            int index = Integer.parseInt(address.substring(tlength, address.length() - olength), 2);
            int tag = Integer.parseInt(address.substring(0, tlength), 2);
            return new int[]{tag, index, offset};
        }

        /**
         * This function is to read the content at the address in the cache.
         * There are two possibilities, either the data is in cache or not.
         * If it is in the cache, the tag matches with the indexed cache line
         * then the read function of the cache line is called and the value is returned.
         * If the required block is not in the cache, a miss is generated.
         * The tag is set as the tag to be read in the appropriate indexed cache line.
         * The block is read from the main memory and written into the appropriate cache line.
         * Then the block is again read from the cache. This time a hit occurs.
         *
         * @param address This  parameter  is the address which has to be Read
         * @return int
         */
        int read(String address) {
            int[] breakAddress = breakAddress(address);
            int tag = breakAddress[0];
            int index = breakAddress[1];
            int offset = breakAddress[2];
            CacheLine line = cache[index];
            if (line.tag != tag) {
                System.out.print("Read Miss ");
                miss++;
                //Bring Data From Main Memory
                //Write It To Cache
                line.tag = tag;
                line.block = MM.read(address);
                //Read It.
            } else {
                System.out.print("Read Hit ");
                hit++;
            }
            return line.read(offset);
        }

        /**
         * This function is to Write the content at the address in the cache.
         * There are two possibilities, either the block is in cache or not.
         * If it is in the cache, the tag matches with the indexed cache line
         * then the write function of the cache line is called and the Data is written.
         * If the required block is not in the cache, a miss is generated.
         * The tag is set as the tag to be read in the appropriate indexed cache line.
         * The block is read from the main memory and written into the appropriate cache line.
         * Then the block is again written in the cache. This time a hit occurs.
         *
         * @param data    Data to be written (Integer)
         * @param address Address where the data is to be written.
         */
        void write(int data, String address) {
            int[] breakAddress = breakAddress(address);
            int tag = breakAddress[0];
            int index = breakAddress[1];
            int offset = breakAddress[2];
            //System.out.println(Arrays.toString(breakAddress));
            CacheLine line = cache[index];
            if (line.tag != tag) {
                System.out.println("Write Miss");
                miss++;
                //Bring Data From Main Memory
                //Write It To Cache
                line.tag = tag;
                //Not really necessary, but just to maintain fluency.
                line.block = MM.read(address);
                //Overwrite data onto it.
            } else {
                System.out.println("Write Hit");
                hit++;
            }
            line.write(data, offset);
            //Write Through
            MM.write(line.block, address);
        }

        /**
         * Prints the current contents of the cache
         */
        void print() {
            System.out.println("Line\tTag\t\tData");
            int ind = 0;
            for (CacheLine cl : cache)
                System.out.println(ind++ + "\t\t" + cl.tag + "\t\t" + Arrays.toString(cl.block));
            System.out.println("Hits - " + hit + "\tMiss - " + miss + "\tHit Ratio - " + (1.0 * hit / (hit + miss)));
            System.out.println();
        }
    }

    /**
     * The Main method takes number of Cache Lines to be in the cache
     * and the number of blocks in each cache lines as the inputs.
     * Then it takes Queries in the form:
     * R <Address>
     * W <Address> <Data>
     * T <Source Address> <Destination Address>
     * P is to print the contents of the cache.
     * E is to exit the queries and stop the execution.
     *
     * @param args IDK
     */
    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);
        System.out.println("Enter The Number of Cache Lines");
        int CL = kb.nextInt();
        System.out.println("Enter The Block Size");
        int B = kb.nextInt();
        kb.nextLine();
        DirectMapped DM = new DirectMapped(B, -1, CL);
        System.out.println("Enter Queries");
        outer:
        while (true) {
            String str = kb.nextLine();
            switch (str.toUpperCase().charAt(0)) {
                case 'E':
                    break outer;
                case 'R':
                    System.out.println(DM.read(str.substring(str.indexOf(' ') + 1)));
                    break;
                case 'W':
                    String addr = str.substring(str.indexOf(' ') + 1, str.lastIndexOf(' '));
                    int data = Integer.parseInt(str.substring(str.lastIndexOf(' ') + 1));
                    DM.write(data, addr);
                    break;
                case 'T':
                    String source = str.substring(str.indexOf(' ') + 1, str.lastIndexOf(' '));
                    String destination = str.substring(str.lastIndexOf(' ') + 1);
                    data = DM.read(source);
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
