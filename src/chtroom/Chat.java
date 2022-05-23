//package chtroom;
//
//public class Chat {
//
//    public Chat() throws InterruptedException {
//        Thread temp = new Thread(new t(5));
//        temp.start();
//        Thread.sleep(5000);
//        temp.notify();
//    }
//
//    public static void main(String[] args) {
//        Chat c = new Chat();
//    }
//
//    private class t implements Runnable {
//
//        protected int limit, current;
//
//        public t(int limit) {
//            super();
//            this.limit = limit;
//            this.current = 0;
//        }
//
//        @Override
//        public void run() {
//            try {
//                System.out.println(current);
//                while (true) {
//                    if (current == limit) {
//                        wait();
//                    }
//                }
//            }
//            catch (InterruptedException e) {
//                System.out.println("Connection interrupted, aborting...");
//                e.printStackTrace();
//            }
//        }
//
//        public void disconnect() {
//            current = current - 1;
//        }
//    }
//
//}