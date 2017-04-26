#!/usr/bin/env python3

import os
import subprocess
import difflib
import sys

ERROR_DIR='my-test-errors'
RESULT_DIR='my-test-results'

# The directories containing the results to compare to
CMP_ERROR_DIR='test-pgm/errors'
CMP_RESULT_DIR='test-pgm/results'

PATH = 'test-pgm'
FILE_EXTENSION = '.pl0'
CLASSPATH = 'bin:java-cup-11b.jar'
MAIN = 'pl0.PL0_LALR'

def set_class_path():
    os.environ['CLASSPATH'] = CLASSPATH

def create_directories():
    if not os.path.isdir(ERROR_DIR):
        os.mkdir(ERROR_DIR)

    if not os.path.isdir(RESULT_DIR):
        os.mkdir(RESULT_DIR)

def get_test_files(path):
    files = os.listdir(path)

    pl0_files = []

    for f in files:
        if f.endswith(FILE_EXTENSION):
            pl0_files.append(f)

    return pl0_files

def run_test(pl0_file, path):
    path_to_pl0_file = os.path.join(path, pl0_file)
    subprocess.call('java {} {} 2> {}/e-{} | tee {}/r-{}'.format(MAIN, path_to_pl0_file, ERROR_DIR, pl0_file, RESULT_DIR, pl0_file), shell=True)

def compare_file(my_test_file, cmp_test_file):
    
    differ = difflib.HtmlDiff()

    html = None

    with open(my_test_file, 'r') as my_file:
        with open(cmp_test_file, 'r') as cmp_file:
            html = differ.make_table(my_file.readlines()[1:],
                    cmp_file.readlines()[1:],
                    fromdesc=my_test_file,
                    todesc=cmp_test_file)

    return html

def create_html_report(filename, html_tables):
    with open(filename, 'w') as f:
        with open('style.html', 'r') as style:
            f.write(style.read())

        f.write('\n'.join(html_tables))

        f.write('</body></html>')

def main():
    set_class_path()
    create_directories()
    pl0_files = get_test_files(PATH)
    for pl0_file in pl0_files:
        run_test(pl0_file, PATH)

    html_tables = []
    for pl0_file in pl0_files:
        html_tables.append(compare_file(os.path.join(RESULT_DIR, 'r-' + pl0_file),
                os.path.join(CMP_RESULT_DIR, 'r-' + pl0_file)))

    create_html_report('output.html', html_tables)
main()
