import requests
import sys
import random
import re

# Regular expression to identify patterns outside of strings and comments
pattern = re.compile(r'"[^"]*"|#.+$|(\/([^\/]*)\/)')

def transform_awk_patterns(awk_content):

    def transform(match):
        if match.group(1):
            return f"`{match.group(2)}`"
        else:
            return match.group()

    # Use the re.sub function to replace / with `
    transformed_content = re.sub(pattern, transform, awk_content)

    return transformed_content


gh_req_url  = lambda x: lambda y: f"https://api.github.com/search/code?q=example+language:awk&?per_page={y}&page={x}" 
download = lambda x, y: requests.get(x, headers={'Authorization': 'token ' + y})
def download_file(json, token):
    url = f"{json['html_url']}?raw=true"
    file = f"tests/normal/{json['name']}"
    file_t = f"tests/backtick/{json['name']}"
    res = download(url, token)
    contents = transform_awk_patterns(res.content.decode("utf-8"))
    open(file_t, 'w').write(f"# source {json['html_url']}\n{contents.encode('utf-8')}")
    open(file, 'w').write(f"# source {json['html_url']}\n{res.content}")

def main():
    if len(sys.argv) != 4:
        print("Usage: python get_awk_files [github-token] [page] [number of files]")
        exit(1)
    if int(sys.argv[3]) < 1 or int(sys.argv[3]) > 100:
            print("Number of files must be between 1 and 100")
            exit(1)
    gh_token = sys.argv[1]
    page = random.randint(1, int(sys.argv[2]))
    file_count = int(sys.argv[3])
    print(f"Downloading {file_count} files from page {page}")
    awk_list = download(gh_req_url(page)(file_count), gh_token).json()["items"]
    print(f"Downloaded {len(awk_list)} files")
    awk_list = [download_file(awk_file, gh_token) for awk_file in awk_list if awk_file['name'].endswith('.awk')]

if __name__ == '__main__':
    main()
