package com.example.jobapi.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJobPosting is a Querydsl query type for JobPosting
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJobPosting extends EntityPathBase<JobPosting> {

    private static final long serialVersionUID = -333745104L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJobPosting jobPosting = new QJobPosting("jobPosting");

    public final QMember author;

    public final BooleanPath cancel = createBoolean("cancel");

    public final StringPath closingDate = createString("closingDate");

    public final StringPath company = createString("company");

    public final StringPath deadline = createString("deadline");

    public final StringPath education = createString("education");

    public final StringPath employmentType = createString("employmentType");

    public final StringPath experience = createString("experience");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath location = createString("location");

    public final StringPath salary = createString("salary");

    public final StringPath sector = createString("sector");

    public final StringPath title = createString("title");

    public final StringPath url = createString("url");

    public final NumberPath<Long> view = createNumber("view", Long.class);

    public QJobPosting(String variable) {
        this(JobPosting.class, forVariable(variable), INITS);
    }

    public QJobPosting(Path<? extends JobPosting> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJobPosting(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJobPosting(PathMetadata metadata, PathInits inits) {
        this(JobPosting.class, metadata, inits);
    }

    public QJobPosting(Class<? extends JobPosting> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new QMember(forProperty("author")) : null;
    }

}

