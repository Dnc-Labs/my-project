package com.ecommerce.api.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // TODO: Thêm relationship cho nested category (danh mục cha/con):
    // - parent: nhiều Category con thuộc 1 Category cha (@ManyToOne, optional)
    // - children: 1 Category cha có nhiều Category con (@OneToMany)

    @OneToMany(mappedBy = "parent")
    private List<Category> children;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
