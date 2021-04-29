select * from domains where name like 'd-201603244.ufsc.br'

select * from records where domain_id in (select id from domains where name like 'd-201603244.ufsc.br') and type = 'A'

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('jmsbroker.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('logging.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('serviceregistry.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('servicefederation.d-201603244.ufsc.br', 'roqbez.sytes.net','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('discoveryservice.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('executionservice.d-201603244.ufsc.br', '192.168.0.14','A','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('executionservice-db.d-201603244.ufsc.br', 'roqbez.sytes.net','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('bpelexporter.d-201603244.ufsc.br', '192.168.0.14','A','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('bindingservice.d-201603244.ufsc.br', '192.168.0.14','A','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('deploymentservice.d-201603244.ufsc.br', '192.168.0.14','A','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('resilienceservice.d-201603244.ufsc.br', 'roqbez.sytes.net','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('catalogservice.d-201603244.ufsc.br', '192.168.0.14','A','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('adhocservices.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('adhocservices2.d-201603244.ufsc.br', 'server2.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('ublservices.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('ublservices2.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('ublservices3.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('ublservices4.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('ublservices5.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('ublservices6.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('server.d-201603244.ufsc.br', '150.162.6.131','A','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('server2.d-201603244.ufsc.br', '150.162.6.133','A','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('server3.d-201603244.ufsc.br', '150.162.6.63','A','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('server4.d-201603244.ufsc.br', '150.162.6.194','A','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('sonar.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('portainer.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('nexus.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

INSERT INTO records (name, content, type, domain_id, ttl, prio, change_date, auth) VALUES ('docker-repo.d-201603244.ufsc.br', 'server.d-201603244.ufsc.br','CNAME','4427',60,0,(SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))),1)

/* Updates */
UPDATE records SET content = 'server.d-201603244.ufsc.br', type = 'CNAME', change_date = (SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))) WHERE name = 'logging.d-201603244.ufsc.br' AND domain_id IN (SELECT id FROM domains WHERE name LIKE 'd-201603244.ufsc.br')
UPDATE records SET content = 'server.d-201603244.ufsc.br', type = 'CNAME', change_date = (SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))) WHERE name = 'discoveryservice.d-201603244.ufsc.br' AND domain_id IN (SELECT id FROM domains WHERE name LIKE 'd-201603244.ufsc.br')
UPDATE records SET content = 'server.d-201603244.ufsc.br', type = 'CNAME', change_date = (SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))) WHERE name = 'jmsbroker.d-201603244.ufsc.br' AND domain_id IN (SELECT id FROM domains WHERE name LIKE 'd-201603244.ufsc.br')

UPDATE records SET content = 'server.d-201603244.ufsc.br', type = 'CNAME', change_date = (SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)))) WHERE name = 'deploymentservice.d-201603244.ufsc.br' AND domain_id IN (SELECT id FROM domains WHERE name LIKE 'd-201603244.ufsc.br')

UPDATE records SET content = 'server.d-201603244.ufsc.br', type = 'CNAME', change_date = (SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4))))  WHERE name like 'ublservices%.d-201603244.ufsc.br' AND domain_id IN (SELECT id FROM domains WHERE name LIKE 'd-201603244.ufsc.br')
UPDATE records SET content = 'roqbez.sytes.net', type = 'CNAME', change_date = (SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4))))  WHERE name = 'executionservice.d-201603244.ufsc.br' AND domain_id IN (SELECT id FROM domains WHERE name LIKE 'd-201603244.ufsc.br')
UPDATE records SET content = 'roqbez.sytes.net', type = 'CNAME', change_date = (SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4))))  WHERE name = 'serviceregistry.d-201603244.ufsc.br' AND domain_id IN (SELECT id FROM domains WHERE name LIKE 'd-201603244.ufsc.br')
UPDATE records SET content = 'roqbez.sytes.net', type = 'CNAME', change_date = (SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4))))  WHERE name = 'servicefederation.d-201603244.ufsc.br' AND domain_id IN (SELECT id FROM domains WHERE name LIKE 'd-201603244.ufsc.br')

UPDATE records SET content = 'roqbez.sytes.net', type = 'CNAME', change_date = (SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4))))  WHERE name = 'resilienceservice.d-201603244.ufsc.br' AND domain_id IN (SELECT id FROM domains WHERE name LIKE 'd-201603244.ufsc.br')

UPDATE records SET content = 'roqbez.sytes.net', type = 'CNAME', change_date = (SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4))))  WHERE name = 'bpelexporter.d-201603244.ufsc.br' AND domain_id IN (SELECT id FROM domains WHERE name LIKE 'd-201603244.ufsc.br')
