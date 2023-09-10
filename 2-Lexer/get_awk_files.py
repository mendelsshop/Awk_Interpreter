import requests;
import sys;
import random;

gh_req_url  = lambda x: f"https://api.github.com/search/code?q=example+language:awk&?per_page={100}&page={x}" 
download = lambda x, y: requests.get(x, headers={'Authorization': 'token ' + y})
def download_file(json, token):
    url = f"{json['html_url']}?raw=true"
    file = f"tests/{json['name']}"
    res = download(url, token)

    open(file, 'wb').write(res.content)

def main():
    page=random.randint(0,100)
    if len(sys.argv) != 3:
        print("Usage: python get_awk_files [github-token] [# of files]")
        exit(1)
    gh_token = sys.argv[1]
    awk_list = download(gh_req_url(page), gh_token).json()["items"]
    awk_list = [download_file(awk_file, gh_token) for awk_file in awk_list if awk_file['name'].endswith('.awk')]

if __name__ == '__main__':
    main()