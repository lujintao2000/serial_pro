# serial_pro
该类库实现了对象的快速序列化/反序列化，专门为java语言打造，后续会支持其它语言。对象序列化后大小约为jdk自带的序列化类库序列化后大小的1/5。
该类库的设计目标：
  1. 小巧，序列化后的字节流占用空间小,平均为kyro的三分之二大小
  2. 处理速度快，序列化/反序列化综合速度为kyro的1.5倍
  3. API调用方便
  4. 支持循环引用
  5. 支持java的所有类型，包括枚举、多维数组
  6. 支持没有无参构造方法的类
  7. 无侵入设计，使用中无需做任何其它配置，只需序列化方/反序列化方类属性的顺序相同
  8. 线程安全
  9. 支持序列化/反序列化的时候类属性不完全一致，但是要求反序列化的时候定义的类属性必须包含序列化时定义的类属性，可以新增属性，新增属性要求在原有属性末尾添加

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
