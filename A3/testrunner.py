#!/usr/bin/env python3
"""
A test runner for COMP4403 assignments

author = "Roy Portas <royportas@gmail.com>"
"""

import os
import subprocess
import difflib
import sys
import platform

# The directories containing your test results
ERROR_DIR='my-test-errors'
RESULT_DIR='my-test-results'

# The directories containing the results to compare to
CMP_ERROR_DIR='test-pgm/errors'
CMP_RESULT_DIR='test-pgm/results'

PATH = 'test-pgm'
FILE_EXTENSION = '.pl0'

# Items on the classpath
PATH_ITEMS = ['bin', 'java-cup-11b.jar']

# The main java class
MAIN = 'pl0.PL0_LALR -c'

def generate_classpath(path_items, working_dir):
    """ Generate the classpath for running a PL0 file """
    # Unix separator
    separator = ':'

    # if platform.system() == 'Windows':
    #     # Windows separator
    #     separator = ';'

    full_path_items = []

    for item in path_items:
        full_path_items.append(os.path.join(working_dir, item))

    classpath = '"' + separator.join(full_path_items) + '"'
    return classpath


def create_directories():
    """ Create the directories if they don't exist """
    if not os.path.isdir(ERROR_DIR):
        os.mkdir(ERROR_DIR)

    if not os.path.isdir(RESULT_DIR):
        os.mkdir(RESULT_DIR)

def get_test_files(path):
    """ Get a list of the test file """
    files = os.listdir(path)

    pl0_files = []

    for f in files:
        if f.endswith(FILE_EXTENSION):
            pl0_files.append(f)

    return pl0_files

def run_test(pl0_file, classpath, path):
    path_to_pl0_file = os.path.join(os.getcwd(), path, pl0_file)
    result_file = os.path.join(os.getcwd(), RESULT_DIR, 'r-{}'.format(pl0_file))
    error_file = os.path.join(os.getcwd(), ERROR_DIR, 'e-{}'.format(pl0_file))

    with open(result_file, 'w') as result_fd:
        with open(error_file, 'w') as error_fd:

            cmd = 'java -cp {} {} {}'.format(classpath, MAIN, path_to_pl0_file)
            print('>>> ' + cmd)

            subprocess.call(cmd,
                stdout=result_fd,
                stderr=error_fd,
                shell=True
            )

    # Print the output from stdout
    with open(result_file, 'r') as result_fd:
        print("Reading file " + result_file)
        print(result_fd.read())

    # subprocess.call('java {} {} 2> {}/e-{} | tee {}/r-{}'.format(MAIN, path_to_pl0_file, ERROR_DIR, pl0_file, RESULT_DIR, pl0_file), shell=True)

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
    classpath = generate_classpath(PATH_ITEMS, os.getcwd())
    create_directories()
    pl0_files = get_test_files(PATH)
    for pl0_file in pl0_files:
        run_test(pl0_file, classpath, PATH)

    html_tables = []
    for pl0_file in pl0_files:
        html_tables.append(compare_file(os.path.join(RESULT_DIR, 'r-' + pl0_file),
                os.path.join(CMP_RESULT_DIR, 'r-' + pl0_file)))

    create_html_report('output.html', html_tables)
main()
