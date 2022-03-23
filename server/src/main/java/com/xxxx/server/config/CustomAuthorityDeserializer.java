package com.xxxx.server.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 自定义反序列化
 * 自定义Authority解析器
 */
public class CustomAuthorityDeserializer extends JsonDeserializer {  //自定义json序列化把authority序列化出来了
    @Override  //这是JsonDeserializer里面反序列化的一个方法
    public Object deserialize(JsonParser jsonParser/*json解析*/, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();//mapper读取解析对象
        JsonNode jsonNode = mapper.readTree(jsonParser);//拿到json的数，
        List<GrantedAuthority> grantedAuthorities=new LinkedList<>();//因为admin返回的是一个集合
        Iterator<JsonNode> elements = jsonNode.elements();//拿到里面的元素（jsonNode中的），获取到的其实是一个迭代器
        while (elements.hasNext()){/*循环展示*/
            JsonNode next = elements.next();
            JsonNode authority =next.get("authority");//通过循环找到authority，authority说明要解析的元素
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.asText()));//将找到的authority放到SimpleGrantedAuthority对象里面
        }
        return grantedAuthorities;
    }
}
