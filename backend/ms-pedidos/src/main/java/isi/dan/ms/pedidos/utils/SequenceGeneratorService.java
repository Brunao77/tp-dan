package isi.dan.ms.pedidos.utils;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    private final MongoOperations mongoOps;

    public long next(String sequenceName) {
        Query q = new Query(Criteria.where("_id").is(sequenceName));
        Update u = new Update().inc("sequence", 1);
        FindAndModifyOptions opt = FindAndModifyOptions.options().returnNew(true).upsert(true);
        CounterSequence counter = mongoOps.findAndModify(q, u, opt, CounterSequence.class);
        return counter.getSequence();
    }

}
