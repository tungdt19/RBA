// package com.viettel.vtag.config;
//
// import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
// import io.r2dbc.postgresql.PostgresqlConnectionFactory;
// import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
//
// import static org.springframework.data.domain.Sort.Order.desc;
// import static org.springframework.data.domain.Sort.by;
// import static org.springframework.data.relational.core.query.Criteria.where;
// import static org.springframework.data.relational.core.query.Query.query;
// import static org.springframework.data.relational.core.query.Update.update;
//
// public class R2dbcConfig {
//     public static void main(String[] args) {
//         var connectionFactory = new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
//             .host("")
//             .database("")
//             .username("")
//             .password("").build());
//         var template = new R2dbcEntityTemplate(connectionFactory);
//
//         var update = template.update(Person.class)
//             .inTable("person_table")
//             .matching(query(where("firstname").is("John")))
//             .apply(update("age", 42));
//
//         var all = template.select(Person.class)
//             .matching(query(where("firstname").is("John")
//                 .and("lastname").in("Doe", "White"))
//                 .sort(by(desc("id"))))
//             .all();
//     }
// }
