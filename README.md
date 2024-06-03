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
The configuration looks like this and can be also found [here](./example.config.yml):

```yaml

commit-message: "Global commit message"  # Optional, defaults to "Sync GitHub files"
entries:
  - commit-message: "Sync README" # Optionally, Overwrites the global commit message
    from: # Copies from The repo Knerio/Knerio the file README.md
      repo: "Knerio/Knerio" # Optionally, if not set, the repository of the action is getting selected
      file: "README.md"
    to: # Pastes as README.md (can also be renamed) in Knerio/GithubSyncFilesAction
      repo: "Knerio/GithubSyncFilesAction" # Optionally, if not set, the repository of the action is getting selected
      file: "showcase/README.md"

  - ignored:
      - "*.config.yml" # You can also exclude files and directories
    from:
      repo: "Knerio/Knerio" # Optionally, if not set, the repository of the action is getting selected
      file: ".github/" # You can also copy a whole directory
    to:
      repo: "Knerio/GithubSyncFilesAction"
      file: "showcase/second/" # Optionally, if not set, the repository of the action is getting selected

```

## Shorthandform

You can also use the short form if you dont need to specify both repositories:

```yaml
entries:
  - from: "README.md" # this uses the default repository, where the action is getting executed
    to: "README"
```
The equivalent would be: 
````yaml
entries:
  - from: 
      file: "README.md"
      repo: "Knerio/GithubSyncFilesAction"
    to: 
      file: "README"
      repo: "Knerio/GithubSyncFilesAction"
````

