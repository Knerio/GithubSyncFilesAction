# Github Sync Files Action

Copies files from a repo to the same or an other repo. <br/>
For example, [showcase/README.md](./showcase/README.md) is synced with [Knerio/Knerio/README.md](https://github.com/Knerio/Knerio/blob/main/README.md)


## Action Setup

```yaml
name: Run the workflow

on:
  schedule:
    - cron: "*/10 * * * *" # Runs every 10 minutes
  workflow_dispatch:

permissions:
  contents: write # This is required

jobs:
  run:
    runs-on: ubuntu-latest
    steps:
      - uses: "actions/checkout@v4"
      - uses: "knerio/GithubSyncFilesAction@main"
        with:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }} #This is optional
          CONFIG: "./.github/sync.config.yml" # This is optional and defaults to "./.github/sync.config.yml"
```

The parameters `GITHUB_TOKEN`, `CONFIG` are optional.

## Configuration

The configuration location is configured above and also can be changed.
The configuration looks like this and can be also found [here](https://github.com/Knerio/GithubSyncFilesAction/example.config.yml):

```yaml

global-commit-message: "Global commit message"  # Optional, defaults to "Sync GitHub files"
entries:
  - commit-message: "Sync README" # Optionally, Overwrites the global commit message
    from: # Copies from The repo Knerio/Knerio the file README.md
      repo: "Knerio/Knerio"
      file: "README.md"
    to: # Pastes as README.md (can also be renamed) in Knerio/GithubSyncFilesAction
      repo: "Knerio/GithubSyncFilesAction"
      file: "showcase/README.md"

  - from:
      repo: "Knerio/Knerio"
      file: ".github/" # You can also copy a whole directory
    to:
      repo: "Knerio/GithubSyncFilesAction"
      file: "showcase/second/"

```
