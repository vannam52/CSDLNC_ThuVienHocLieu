import os
from pathlib import Path

import psycopg2
from psycopg2 import sql


def database_config():
    return {
        'dbname': os.getenv('LIBRARY_DB_NAME', 'library_management'),
        'user': os.getenv('LIBRARY_DB_USER', 'postgres'),
        'password': os.getenv('LIBRARY_DB_PASSWORD', '12345'),
        'host': os.getenv('LIBRARY_DB_HOST', 'localhost'),
        'port': os.getenv('LIBRARY_DB_PORT', '5432'),
    }


def setup_db():
    config = database_config()
    name = config['dbname']
    if name in ('postgres', 'template0', 'template1'):
        raise ValueError('Hãy chọn một CSDL riêng cho đồ án.')

    admin = psycopg2.connect(**{**config, 'dbname': 'postgres'})
    try:
        admin.autocommit = True
        with admin.cursor() as cursor:
            cursor.execute('SELECT 1 FROM pg_database WHERE datname = %s', (name,))
            if cursor.fetchone() is None:
                cursor.execute(sql.SQL('CREATE DATABASE {}').format(sql.Identifier(name)))
    finally:
        admin.close()

    resources = Path(__file__).resolve().parents[1] / 'src' / 'main' / 'resources'
    files = [
        '01_schema.sql',
        '02_data_sample.sql',
        '04_transaction_procedure.sql',
        '05_triggers_and_functions.sql',
        '06_thuc_nghiem_de_tai_3.sql',
    ]
    connection = psycopg2.connect(**config)
    try:
        # Khôi phục schema cũ nếu bất kỳ script nào thất bại.
        with connection, connection.cursor() as cursor:
            cursor.execute('DROP SCHEMA public CASCADE')
            cursor.execute('CREATE SCHEMA public')
            for filename in files:
                cursor.execute((resources / filename).read_text(encoding='utf-8'))
                print(f'Đã chạy {filename}')
    finally:
        connection.close()

    print(f'Khởi tạo xong CSDL {name}.')


if __name__ == '__main__':
    setup_db()
