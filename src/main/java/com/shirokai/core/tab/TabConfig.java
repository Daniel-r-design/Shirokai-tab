package com.shirokai.core.tab;

import com.google.gson.*;
import com.shirokai.core.styles.JsonValidation;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public record TabConfig(int schemaVersion,int updateTicks,boolean managePlayerNames,boolean manageTeams,
                        String nameTemplate,List<Layout> layouts,List<NameRule> nameRules){
    public record Condition(String permission,String group,Set<String> worlds){
        public Condition{worlds=Set.copyOf(worlds);if(permission!=null&&!permission.matches("[a-zA-Z0-9_.-]{1,128}")||group!=null&&!group.matches("[a-zA-Z0-9_-]{1,64}")||worlds.size()>50||worlds.stream().anyMatch(w->!w.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")))throw new IllegalArgumentException("Condição de TAB inválida");}
        public static Condition any(){return new Condition(null,null,Set.of());}
    }
    public record Layout(String id,int priority,Condition condition,List<String> header,List<String> footer){
        public Layout{validate(id,priority,condition);header=List.copyOf(header);footer=List.copyOf(footer);if(header.size()>12||footer.size()>12)throw new IllegalArgumentException("Máximo de12 linhas por header/footer");}
    }
    public record NameRule(String id,int priority,Condition condition,String template){public NameRule{validate(id,priority,condition);Objects.requireNonNull(template);}}
    private static void validate(String id,int priority,Condition condition){if(id==null||!id.matches("[a-z][a-z0-9_-]{0,63}")||priority < -1000000||priority>1000000||condition==null)throw new IllegalArgumentException("Layout/regra de TAB inválida");}
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public TabConfig{
        layouts=List.copyOf(layouts);nameRules=List.copyOf(nameRules);if(schemaVersion!=1||updateTicks<5||updateTicks>1200||nameTemplate==null||layouts.isEmpty()||layouts.size()>50||nameRules.size()>50)throw new IllegalArgumentException("Configuração de TAB inválida");
        var ids=new HashSet<String>();for(var layout:layouts)if(!ids.add(layout.id()))throw new IllegalArgumentException("Layout duplicado");ids.clear();for(var rule:nameRules)if(!ids.add(rule.id()))throw new IllegalArgumentException("Regra duplicada");
        if(layouts.stream().noneMatch(l->l.id().equals("default")&&l.condition().equals(Condition.any())))throw new IllegalArgumentException("Layout default sem condições é obrigatório");
    }
    public static TabConfig defaults(){return new TabConfig(1,20,true,false,"%shirokai:title_prefix%%shirokai:player%",List.of(new Layout("default",0,Condition.any(),
            List.of("%animation:gold_wave%","<gray>Explore • Capture • Battle</gray>","<aqua>Online: %shirokai:online%</aqua>"),List.of("<yellow>Bem-vindo, %shirokai:player%!</yellow>","<gray>Clã: %shirokai:clan%</gray>"))),List.of());}
    public static TabConfig load(Path file)throws IOException{
        Files.createDirectories(file.getParent());if(!Files.exists(file))try{Files.writeString(file,GSON.toJson(defaults())+"\n",StandardOpenOption.CREATE_NEW);}catch(FileAlreadyExistsException ignored){}
        if(Files.size(file)>262144)throw new IOException("tab.json excede256KiB");try{return parse(Files.readString(file));}catch(Exception error){throw new IOException(file+": "+error.getMessage(),error);}
    }
    public static TabConfig parse(String input)throws IOException{
        var root=JsonValidation.object(input);JsonValidation.fields(root,Set.of("schemaVersion","updateTicks","managePlayerNames","manageTeams","nameTemplate","layouts","nameRules"));
        JsonValidation.integer(root,"schemaVersion");JsonValidation.integer(root,"updateTicks");JsonValidation.bool(root,"managePlayerNames");JsonValidation.bool(root,"manageTeams");JsonValidation.string(root,"nameTemplate",false);
        for(String field:List.of("layouts","nameRules")){
            if(!root.has(field)||!root.get(field).isJsonArray())throw new IllegalArgumentException(field+" deve ser lista");
            for(var item:root.getAsJsonArray(field)){
                if(!item.isJsonObject())throw new IllegalArgumentException("Regra deve ser objeto");var object=item.getAsJsonObject();
                JsonValidation.fields(object,field.equals("layouts")?Set.of("id","priority","condition","header","footer"):Set.of("id","priority","condition","template"));JsonValidation.string(object,"id",false);JsonValidation.integer(object,"priority");
                if(!object.has("condition")||!object.get("condition").isJsonObject())throw new IllegalArgumentException("condition deve ser objeto");var condition=object.getAsJsonObject("condition");JsonValidation.fields(condition,Set.of("permission","group","worlds"));JsonValidation.string(condition,"permission",true);JsonValidation.string(condition,"group",true);JsonValidation.strings(condition,"worlds");
                if(field.equals("layouts")){JsonValidation.strings(object,"header");JsonValidation.strings(object,"footer");}else JsonValidation.string(object,"template",false);
            }
        }
        return GSON.fromJson(root,TabConfig.class);
    }
    public boolean requiresRestartComparedTo(TabConfig previous){return managePlayerNames!=previous.managePlayerNames||manageTeams!=previous.manageTeams;}
}
