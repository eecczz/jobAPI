package com.example.jobapi.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSaveList is a Querydsl query type for SaveList
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSaveList extends EntityPathBase<SaveList> {

    private static final long serialVersionUID = -949175834L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSaveList saveList = new QSaveList("saveList");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QJobPosting jobPosting;

    public final QMember member;

    public QSaveList(String variable) {
        this(SaveList.class, forVariable(variable), INITS);
    }

    public QSaveList(Path<? extends SaveList> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSaveList(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSaveList(PathMetadata metadata, PathInits inits) {
        this(SaveList.class, metadata, inits);
    }

    public QSaveList(Class<? extends SaveList> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.jobPosting = inits.isInitialized("jobPosting") ? new QJobPosting(forProperty("jobPosting"), inits.get("jobPosting")) : null;
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

