# notion-commands-server

FIXME: description

## Build

```bash
docker buildx build --platform linux/amd64 -t puelle/notion-commands . \
  && docker push puelle/notion-commands:latest
```

## Usage

```bash
docker pull puelle/notion-commands:latest

docker run --hostname notion.puelle.me \
  -e HOSTNAME=notion.puelle.me \
  -p 8080:80 \
  -it -d --rm \
  -v `pwd`:/var/log/exported \
  puelle/notion-commands
```
