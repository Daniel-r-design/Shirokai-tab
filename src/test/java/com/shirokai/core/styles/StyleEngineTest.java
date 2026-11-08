package com.shirokai.core.styles;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StyleEngineTest{
    @Test void namedHexNestedStylesAndUnicodeGradientAreRealComponents(){
        var engine=new StyleEngine(StyleConfig.defaults());var text=engine.render(engine.compile("<bold><#123456>A</#123456>B</bold>C"),key->"",0,true);
        assertEquals("ABC",text.getString());assertEquals(0x123456,text.getSiblings().get(0).getStyle().getColor().getRgb());assertTrue(text.getSiblings().get(1).getStyle().isBold());assertFalse(text.getSiblings().get(2).getStyle().isBold());
        var gradient=engine.render(engine.compile("<gradient:#ff0000:#0000ff>A😀B</gradient>"),key->"",0,true);
        assertEquals("A😀B",gradient.getString());assertEquals(3,gradient.getSiblings().size());assertEquals(0xff0000,gradient.getSiblings().getFirst().getStyle().getColor().getRgb());assertEquals(0x0000ff,gradient.getSiblings().getLast().getStyle().getColor().getRgb());
    }
    @Test void placeholderValuesAreLiteralAndNeverReparsed(){
        var engine=new StyleEngine(StyleConfig.defaults());var text=engine.render(engine.compile("<gold>%shirokai:player%</gold>"),key->"<click:run_command:'/op x'>%animation:gold_wave%</click>",0,true);
        assertTrue(text.getString().startsWith("<click:"));assertTrue(text.getString().contains("%animation:gold_wave%"));assertNull(text.getSiblings().getFirst().getStyle().getClickEvent());assertEquals(0xffaa00,text.getSiblings().getFirst().getStyle().getColor().getRgb());
    }
    @Test void unsupportedMalformedAndRecursiveTemplatesAreRejected(){
        var engine=new StyleEngine(StyleConfig.defaults());
        for(String source:List.of("<click:run_command:/op>x</click>","<hover:x>x</hover>","<gold>x</red>","<bold>x","</bold>","<gradient:#bad:#ffffff>x</gradient>","%animation:missing%","x".repeat(2049)))assertThrows(IllegalArgumentException.class,()->engine.compile(source));
        assertThrows(IllegalArgumentException.class,()->new StyleEngine(new StyleConfig(1,Map.of("loop",new StyleConfig.Animation(5,List.of("%animation:loop%"))))));
        assertEquals("<red>",engine.render(engine.compile("\\<red>"),key->"",0,true).getString());
    }
    @Test void animationClockFreezeAndRenderCacheAreBounded(){
        var engine=new StyleEngine(new StyleConfig(1,Map.of("test",new StyleConfig.Animation(5,List.of("<red>A</red>","<blue>B</blue>")))));
        var template=engine.compile("%animation:test% %shirokai:player%");var a=engine.render(template,key->"Alice",0,true);
        assertSame(a,engine.render(template,key->"Alice",4,true));assertEquals("B Alice",engine.render(template,key->"Alice",5,true).getString());assertEquals("A Alice",engine.render(template,key->"Alice",5,false).getString());
        for(int i=0;i<1500;i++){String name="Player"+i;engine.render(template,key->name,0,true);}assertTrue(engine.cachedRenders()<=1024);
    }
    @Test void expandedTextAndDynamicControlCharactersAreBounded(){
        var engine=new StyleEngine(StyleConfig.defaults());assertThrows(IllegalArgumentException.class,()->engine.render(engine.compile("%shirokai:player%"),key->"x".repeat(513),0,false));
        assertThrows(IllegalArgumentException.class,()->engine.render(engine.compile("%shirokai:player%"),key->"line\nbreak",0,false));
        assertThrows(IllegalArgumentException.class,()->engine.render(engine.compile("%shirokai:player%".repeat(5)),key->"x".repeat(512),0,false));
    }
}
