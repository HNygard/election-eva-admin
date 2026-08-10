# NF-014 — The toolchain cannot reach Docker

Status: FIXED
Milestone: M1

## Resolution

The repo owner added the user to the `docker` group. `getent group docker` then
showed `docker:x:124:hallvard`, but `id -nG` in an already-running shell still did
not list it — group membership is fixed at process start, so existing sessions
never pick it up. `newgrp docker` does not help either, because it only affects
the shell it spawns, not other processes.

Two ways through:

- **New login session** — the clean fix. Anything started afterwards has the group.
- **`sg docker -c "<command>"`** — runs a single command with the group applied. Works without a password once the user is genuinely a member, which is what unblocked this session:

      $ sg docker -c "./tools/mvn.sh -version"
      Apache Maven 3.8.6
      Java version: 1.8.0_342, vendor: Oracle Corporation

`sg` is a bridge for a stale session, not something to bake into `tools/`. In a
fresh session the tools work directly.

## Symptom

    $ ./tools/mvn.sh -version
    Creating Maven repository volume 'eva-admin-maven-repo'
    permission denied while trying to connect to the docker API at unix:///var/run/docker.sock

Every container-based tool is unusable: `tools/mvn.sh`, `tools/build.sh`,
`tools/seed-local-repo.sh`, and `docker/postgres/`. Since D003 puts the entire
toolchain in containers, this blocks all of M1 and M2.

## What is known

    $ id
    uid=1000(hallvard) gid=1000(hallvard) groups=1000(hallvard),4(adm),24(cdrom),
    27(sudo),30(dip),46(plugdev),100(users),114(lpadmin)

    $ ls -l /var/run/docker.sock
    srw-rw---- 1 root docker 0 Aug  3 11:22 /var/run/docker.sock

The user is in `sudo` but not in `docker`, so the socket is unreachable. Sudo
requires a password, so an agent cannot escalate. There is no rootless daemon
either — `$XDG_RUNTIME_DIR/docker.sock` does not exist and `DOCKER_HOST` is unset.

The daemon itself is running and reachable for a privileged caller; this is purely
about group membership.

## How to fix

The repo owner runs one of these. In Claude Code, prefix with `!` so the output
lands in the session:

    ! sudo usermod -aG docker $USER

then log out and back in, or start a new login session with `newgrp docker`. The
group change does not apply to already-running shells, which is the usual reason
this appears not to have worked.

Verify with:

    ./tools/mvn.sh -version

Rootless Docker is the alternative if adding the user to `docker` is unwanted —
that group is effectively root on the host.

## Done when

`./tools/mvn.sh -version` prints a Maven version.
