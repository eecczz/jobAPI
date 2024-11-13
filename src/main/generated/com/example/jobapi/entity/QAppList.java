package com.example.jobapi.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAppList is a Querydsl query type for AppList
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAppList extends EntityPathBase<AppList> {

    private static final long serialVersionUID = -1589224748L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAppList appList = new QAppList("appList");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QJobPosting jobPosting;

    public final QMember member;

    public QAppList(String variable) {
        this(AppList.class, forVariable(variable), INITS);
    }

    public QAppList(Path<? extends AppList> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAppList(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAppList(PathMetadata metadata, PathInits inits) {
        this(AppList.class, metadata, inits);
    }

    public QAppList(Class<? extends AppList> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.jobPosting = inits.isInitialized("jobPosting") ? new QJobPosting(forProperty("jobPosting"), inits.get("jobPosting")) : null;
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

