insert into process_instance (id, last_modified, last_modified_from, last_modified_to, process_definition_id, status, initiator, start_date, business_key, process_definition_key) values
  ('0', CURRENT_TIMESTAMP, null, null, 'process_definition_id', 'RUNNING', 'initiator',null, 'bus_key','def_key'),
  ('1', CURRENT_TIMESTAMP, null, null, 'process_definition_id', 'RUNNING', 'initiator',null, 'bus_key','def_key');

insert into task (id, assignee, category, created_date, description, due_date, last_modified, last_modified_from, last_modified_to, name, priority, process_definition_id, process_instance_id, status, owner, claimed_date) values
  ('1', 'assignee', 'category', CURRENT_TIMESTAMP, 'description', null, null, null, null, 'task1', 5, 'process_definition_id', 0, 'COMPLETED'  , 'owner', null),
  ('2', 'assignee', 'category', CURRENT_TIMESTAMP, 'description', null, null, null, null, 'task2', 10, 'process_definition_id', 0, 'CREATED'  , 'owner', null),
  ('3', 'assignee', 'category', CURRENT_TIMESTAMP, 'description', null, null, null, null, 'task3', 5, 'process_definition_id', 0, 'CREATED'  , 'owner', null),
  ('4', 'assignee', 'category', CURRENT_TIMESTAMP, 'description', null, null, null, null, 'task4', 10, 'process_definition_id', 1, 'CREATED'  , 'owner', null),
  ('5', 'assignee', 'category', CURRENT_TIMESTAMP, 'description', null, null, null, null, 'task5', 5, 'process_definition_id', 1, 'COMPLETED'  , 'owner', null);

insert into variable (create_time, execution_id, last_updated_time, name, process_instance_id, task_id, type, value) values
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable1', 0, '1', 'String', '{"value": 10}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable2', 0, '1', 'String', '{"value": 10}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable3', 0, '2', 'String', '{"value": 10}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable4', 0, '2', 'String', '{"value": 10}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable5', 1, '4', 'String', '{"value": 10}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable6', 1, '4', 'String', '{"value": 10}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'initiator', 1, null, 'String', '{"value": 10}');
