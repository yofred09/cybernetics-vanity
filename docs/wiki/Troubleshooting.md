# Troubleshooting

## The menu does not open

- Assign the menu key under Controls.
- Confirm the Vanity Implant is installed, unless the server disabled `requireVanityImplant`.
- Check the server's `allowedPermissionLevel`.

## Another player cannot see my choices

Confirm that the same Vanity version is installed on the server and both clients. Reconnect after changing the server mod list.

## CPM models, Sandevistan, or Holoprojector problems

These renderers are handled by [CPM Visual Bridge](https://github.com/yofred09/cpm-visual-bridge), not Vanity. Confirm that CPM Visual Bridge supports the target mod and report rendering issues in its repository with the CPM profile type, exact mod versions, and a short recording.

## Reporting a bug

Include `latest.log`, the exact mod versions, reproduction steps, the affected implant, and whether the issue appears in first or third person. Remove access tokens, server addresses, and other private information before attaching logs.
