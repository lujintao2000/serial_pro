# serial_pro
Serialize/unserialize object
该类库实现了对象的序列化/反序列化，专门为java语言打造，对象序列化后大小约为jdk自带的序列化类库序列化后大小的1/5。
该类库的设计目标：
  1. 小巧，序列化后的字节流占用空间小
  2. 处理速度快
  3. API调用方便

使用方法如下：
        //序列化对象
        OutputStream output2 = new FileOutputStream("e:\\list.obj");
        ObjectOutputStream out = new DefaultObjectOutputStream(output2, needOrder,false);
        //调用该方法实现序列化
        out.write(originalValue);
        out.close();
        
        //反序列化
        boolean needOrder = true;
        ObjectInputStream in = new DefaultObjectInputStream(new FileInputStream("e:\\list.obj"),needOrder,false);
        Object obj = null;
        try {
            //调用该方法实现反序列化
            obj = in.readObject();
            System.out.println(obj);
        } catch (InvalidDataFormatException e) {
            e.printStackTrace();
        }finally {
            in.close();
        }
