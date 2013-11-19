package com.mangofactory.swagger.mixins

import com.mangofactory.swagger.dummy.DummyClass
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

import static com.mangofactory.swagger.dummy.DummyClass.*

class RequestMappingSupport {

   def requestMappingInfo(String path){
      PatternsRequestCondition patterns = new PatternsRequestCondition([path] as String[])
      new RequestMappingInfo(patterns, null, null, null, null, null, null)
   }

   def dummyHandlerMethod(){
      def clazz = new DummyClass()
      Class c = clazz.getClass();
      new HandlerMethod(clazz, c.getMethod("dummyMethod", null))
   }

   def ignorableHandlerMethod(){
      def clazz = new DummyClass.ApiIgnorableClass()
      Class c = clazz.getClass();
      new HandlerMethod(clazz, c.getMethod("dummyMethod", null))
   }

}