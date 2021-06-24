package org.acme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Indexed
@Table(name="Book")
public class Book extends PanacheEntity {

    @FullTextField(analyzer = "english")
    @Column(length = 200)
    public String title;

    @ManyToOne
    @JsonIgnore
    public Author author;




}