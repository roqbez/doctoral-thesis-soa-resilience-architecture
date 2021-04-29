
    alter table connection 
        drop constraint FKD1C47EDE1B2F4573

    alter table connection 
        drop constraint FKD1C47EDEC9BF4C47

    alter table connection 
        drop constraint FKD1C47EDE93E5A5B

    alter table decision 
        drop constraint FK21B82FDC93E5A5B

    alter table document 
        drop constraint FK335CD11BB8D9131E

    alter table end_event 
        drop constraint FK73930E3693E5A5B

    alter table fork 
        drop constraint FK300CC293E5A5B

    alter table input_branch_input_contact_points 
        drop constraint FK8917AA7F1B2F4573

    alter table input_branch_input_contact_points 
        drop constraint FK8917AA7F12B7A88

    alter table input_contact_point 
        drop constraint FKF55504FCFD193459

    alter table junction 
        drop constraint FKF1D4275493E5A5B

    alter table merge 
        drop constraint FK62FA43893E5A5B

    alter table output_branch_output_contact_points 
        drop constraint FKE76F04817810187E

    alter table output_branch_output_contact_points 
        drop constraint FKE76F0481C9BF4C47

    alter table output_contact_point 
        drop constraint FKA3E8D9F3FD193459

    alter table participant 
        drop constraint FK2DBDEF33B8D9131E

    alter table process 
        drop constraint FKED8D1E6F2A301F9D

    alter table process 
        drop constraint FK363585FA0B7E8Eed8d1e6f

    alter table process_tree_description 
        drop constraint FK64FEE8AB5CD53D0

    alter table process_tree_description 
        drop constraint FK64FEE8AB4642609C

    alter table resource 
        drop constraint FKEBABC40EB8D9131E

    alter table start_event 
        drop constraint FKA46D487D93E5A5B

    alter table task 
        drop constraint FK363585FA0B7E8E

    alter table task_participant 
        drop constraint FK724B6379772AF563

    alter table task_resource 
        drop constraint FKEC2E25886DB71A3F

    drop table connection if exists

    drop table decision if exists

    drop table document if exists

    drop table end_event if exists

    drop table fork if exists

    drop table input_branch if exists

    drop table input_branch_input_contact_points if exists

    drop table input_contact_point if exists

    drop table junction if exists

    drop table merge if exists

    drop table output_branch if exists

    drop table output_branch_output_contact_points if exists

    drop table output_contact_point if exists

    drop table participant if exists

    drop table process if exists

    drop table process_category if exists

    drop table process_standard if exists

    drop table process_tree_description if exists

    drop table qos_criterion if exists

    drop table resource if exists

    drop table service_association if exists

    drop table start_event if exists

    drop table task if exists

    drop table task_participant if exists

    drop table task_resource if exists
