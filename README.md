# serial_pro
Serialize/unserialize object
该类库实现了对象的序列化/反序列化，专门为java语言打造，对象序列化后大小约为jdk自带的序列化类库序列化后大小的1/5。
该类库的设计目标：
  1. 小巧，序列化后的字节流占用空间小,平均为kyrkyro1.5
  2. 处理速度快，序列化/反序列化综合速度为kyro的1.5
  3. API调用方便
  4. 支持循环引用
  5. 支持java的所有类型，包括没有公共构造方法的类
  6. 无侵入式设计，使用中无需做任何其它配置，对原有类设计没有任何要求

使用方法如下：
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        //序列化对象
        Serial.write(originalValue,output);
        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        Object obj = null;
        try {
            //反序列化
            obj = Serial.read(input);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            input.close();
        }
