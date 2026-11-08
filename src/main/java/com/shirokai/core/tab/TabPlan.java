package com.shirokai.core.tab;

import com.shirokai.core.permissions.PermissionService;
import com.shirokai.core.styles.StyleEngine;
import java.util.*;
import java.util.function.Predicate;

/** Fully compiled and validated before publishing a reload. */
public final class TabPlan{
    public record Header(TabConfig.Layout definition,List<StyleEngine.Compiled> header,List<StyleEngine.Compiled> footer){}
    public record Name(TabConfig.NameRule definition,StyleEngine.Compiled template){}
    private final TabConfig config;private final StyleEngine styles;private final List<Header> headers;private final List<Name> names;private final StyleEngine.Compiled defaultName;
    public TabPlan(TabConfig config,StyleEngine styles){
        this.config=config;this.styles=styles;defaultName=styles.compile(config.nameTemplate());
        headers=config.layouts().stream().sorted(Comparator.comparingInt(TabConfig.Layout::priority).reversed().thenComparing(TabConfig.Layout::id))
            .map(layout->new Header(layout,layout.header().stream().map(styles::compile).toList(),layout.footer().stream().map(styles::compile).toList())).toList();
        names=config.nameRules().stream().sorted(Comparator.comparingInt(TabConfig.NameRule::priority).reversed().thenComparing(TabConfig.NameRule::id)).map(rule->new Name(rule,styles.compile(rule.template()))).toList();
        if(styles.literalSize(defaultName)>256||names.stream().anyMatch(n->styles.literalSize(n.template())>256))throw new IllegalArgumentException("Nome estático excede256 caracteres");
        for(var header:headers)if(header.header().stream().mapToInt(styles::literalSize).sum()>2048||header.footer().stream().mapToInt(styles::literalSize).sum()>2048)throw new IllegalArgumentException("Header/footer estático excede2048 caracteres");
    }
    public static boolean matches(TabConfig.Condition condition,PermissionService.Metadata metadata,String world,Predicate<String> permissions){
        return (condition.permission()==null||permissions.test(condition.permission()))&&(condition.group()==null||condition.group().equalsIgnoreCase(metadata.group()))&&(condition.worlds().isEmpty()||condition.worlds().contains(world));
    }
    public Header header(PermissionService.Metadata metadata,String world,Predicate<String> permissions){return headers.stream().filter(h->matches(h.definition().condition(),metadata,world,permissions)).findFirst().orElseThrow();}
    public Optional<Name> name(PermissionService.Metadata metadata,String world,Predicate<String> permissions){return names.stream().filter(n->matches(n.definition().condition(),metadata,world,permissions)).findFirst();}
    public StyleEngine.Compiled defaultName(){return defaultName;}public TabConfig config(){return config;}public StyleEngine styles(){return styles;}
}
