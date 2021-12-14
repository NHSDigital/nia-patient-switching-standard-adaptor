CREATE OR REPLACE FUNCTION audit.create_audit_entry_function() RETURNS trigger AS '
DECLARE
v_old_data TEXT;
v_new_data TEXT;
BEGIN
if (TG_OP = ''UPDATE'') then
v_old_data := ROW(OLD.*);
v_new_data := ROW(NEW.*);
insert into audit.logged_actions (schema_name,table_name,user_name,action,original_data,new_data,query)
values (TG_TABLE_SCHEMA::TEXT,TG_TABLE_NAME::TEXT,session_user::TEXT,substring(TG_OP,1,1),v_old_data,v_new_data, current_query());
RETURN NEW;
elsif (TG_OP = ''DELETE'') then
v_old_data := ROW(OLD.*);
insert into audit.logged_actions (schema_name,table_name,user_name,action,original_data,query)
values (TG_TABLE_SCHEMA::TEXT,TG_TABLE_NAME::TEXT,session_user::TEXT,substring(TG_OP,1,1),v_old_data, current_query());
RETURN OLD;
elsif (TG_OP = ''INSERT'') then
v_new_data := ROW(NEW.*);
insert into audit.logged_actions (schema_name,table_name,user_name,action,new_data,query)
values (TG_TABLE_SCHEMA::TEXT,TG_TABLE_NAME::TEXT,session_user::TEXT,substring(TG_OP,1,1),v_new_data, current_query());
RETURN NEW;
else
RAISE WARNING ''[AUDIT.CREATE_AUDIT_ENTRY_FUNCTION] - Other action occurred: %, at %'',TG_OP,now();
RETURN NULL;
end if;

EXCEPTION
WHEN data_exception THEN
RAISE WARNING ''[AUDIT.CREATE_AUDIT_ENTRY_FUNCTION] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %'',SQLSTATE,SQLERRM;
RETURN NULL;
WHEN unique_violation THEN
RAISE WARNING ''[AUDIT.CREATE_AUDIT_ENTRY_FUNCTION] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %'',SQLSTATE,SQLERRM;
RETURN NULL;
WHEN others THEN
RAISE WARNING ''[AUDIT.CREATE_AUDIT_ENTRY_FUNCTION] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %'',SQLSTATE,SQLERRM;
RETURN NULL;
END;'
LANGUAGE plpgsql SECURITY DEFINER
SET search_path = pg_catalog, audit;

CREATE TRIGGER patient_migration_request_if_modified_trigger
AFTER INSERT OR UPDATE OR DELETE ON public.patient_migration_request
FOR EACH ROW EXECUTE PROCEDURE audit.create_audit_entry_function();
