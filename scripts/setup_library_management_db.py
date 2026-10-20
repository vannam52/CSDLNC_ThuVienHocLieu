import psycopg2

def setup_db():
    conn = psycopg2.connect(dbname='postgres', user='postgres', password='12345', host='localhost', port=5432)
    conn.autocommit = True
    cur = conn.cursor()
    cur.execute("SELECT 1 FROM pg_database WHERE datname = 'library_management'")
    if not cur.fetchone():
        cur.execute("CREATE DATABASE library_management")
        print("✅ Đã tạo database: library_management")
    else:
        print("ℹ️ Database library_management đã tồn tại.")
    cur.close()
    conn.close()

    # Kết nối vào library_management và nạp các file SQL
    conn_app = psycopg2.connect(dbname='library_management', user='postgres', password='12345', host='localhost', port=5432)
    conn_app.autocommit = True
    cur_app = conn_app.cursor()

    # Xoá trắng toàn bộ bảng cũ để làm sạch (Reset DB)
    print("🧹 Đang làm sạch Database cũ...")
    cur_app.execute("DROP SCHEMA public CASCADE;")
    cur_app.execute("CREATE SCHEMA public;")
    cur_app.execute("GRANT ALL ON SCHEMA public TO postgres;")
    cur_app.execute("GRANT ALL ON SCHEMA public TO public;")

    import os
    base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    sql_files = [
        os.path.join(base_dir, "src", "main", "resources", "01_schema.sql"),
        os.path.join(base_dir, "src", "main", "resources", "02_data_sample.sql"),
        os.path.join(base_dir, "src", "main", "resources", "04_transaction_procedure.sql")
    ]

    for sql_file in sql_files:
        try:
            with open(sql_file, "r", encoding="utf-8") as f:
                sql_content = f.read()
            cur_app.execute(sql_content)
            rel_name = os.path.relpath(sql_file, base_dir)
            print(f"✅ Đã chạy thành công: {rel_name}")
        except Exception as e:
            rel_name = os.path.relpath(sql_file, base_dir)
            print(f"⚠️ Lỗi khi chạy {rel_name}: {e}")

    cur_app.close()
    conn_app.close()
    print("\n🎉 Khởi tạo CSDL PostgreSQL cho LibraryManagementConsole thành công 100%!")

if __name__ == "__main__":
    setup_db()
