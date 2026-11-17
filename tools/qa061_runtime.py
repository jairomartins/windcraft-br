"""Local release-JAR QA harness. Never reads launcher accounts or tokens.

Only cached public version metadata/libraries/assets are reused. All game dirs,
worlds, logs and settings are isolated beneath qa/061. No production code injected.
"""
from pathlib import Path
import argparse, json, os, shutil, socket, struct, subprocess, uuid

ROOT = Path(__file__).resolve().parents[1]
QA = ROOT / 'qa' / '061'
MC = Path(os.environ['APPDATA']) / '.minecraft'
JAVA = Path(os.environ['JAVA_HOME']) / 'bin' / 'java.exe'

def allowed(lib):
    result = not lib.get('rules')
    for rule in lib.get('rules', []):
        system = rule.get('os', {})
        if system.get('name', 'windows') != 'windows': continue
        if system.get('arch', 'x86_64') not in ('x86_64', 'amd64'): continue
        if rule.get('features'): continue
        result = rule['action'] == 'allow'
    return result

def prepare():
    vanilla = json.loads((MC/'versions/26.3/26.3.json').read_text())
    forge = json.loads((MC/'versions/26.3-forge-66.1.8/26.3-forge-66.1.8.json').read_text())
    libraries = {}
    for lib in vanilla['libraries'] + forge['libraries']:
        if not allowed(lib): continue
        artifact = lib.get('downloads', {}).get('artifact')
        if not artifact: continue
        key = ':'.join(lib['name'].split(':')[:2])
        if len(lib['name'].split(':')) > 3: key += ':' + lib['name'].split(':')[3]
        libraries[key] = artifact['path']
    paths = []
    for rel in libraries.values():
        source = MC/'libraries'/rel
        if not source.is_file(): raise FileNotFoundError(source)
        dest = QA/'client-libraries'/rel
        dest.parent.mkdir(parents=True, exist_ok=True)
        if not dest.exists(): shutil.copy2(source, dest)
        paths.append(str(dest))
    paths.append(str(MC/'versions/26.3/26.3.jar'))
    for name in ['client-a', 'client-b', 'server']:
        directory = QA/name
        (directory/'mods').mkdir(parents=True, exist_ok=True)
        shutil.copy2(ROOT/'dist/windcraft-0.6.1.jar', directory/'mods/windcraft-0.6.1.jar')
        if name.startswith('client'):
            (directory/'options.txt').write_text('renderDistance:4\nsimulationDistance:4\nmaxFps:30\nenableVsync:false\nguiScale:2\nlang:pt_br\nonboardAccessibility:false\nshowSubtitles:true\npauseOnLostFocus:false\nautoJump:false\n', encoding='utf-8')
    (QA/'classpath.json').write_text(json.dumps(paths), encoding='utf-8')
    server=QA/'server'
    (server/'eula.txt').write_text('eula=true\n')
    (server/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25575\nonline-mode=false\nenable-rcon=true\nrcon.port=25585\nrcon.password=windcraft-local-qa\nlevel-name=qa-world\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}\ngamemode=creative\ndifficulty=peaceful\nspawn-protection=0\nview-distance=4\nsimulation-distance=4\nmax-players=3\nsync-chunk-writes=false\n', encoding='utf-8')
    print('Prepared isolated dirs with release JAR and cached libraries; no accounts accessed.')

def start_client(name):
    directory=QA/('client-a' if name=='WCTestA' else 'client-b')
    native=directory/'natives'
    args=['-Xms256m','-Xmx1200m','-XX:+UseG1GC','-XX:+UseCompactObjectHeaders','-XX:StackShadowPages=32','--enable-native-access=ALL-UNNAMED','--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED',
          f'-Djava.library.path={native}/java',f'-Djna.tmpdir={native}/jna',f'-Dorg.lwjgl.system.SharedLibraryExtractPath={native}/lwjgl',f'-Dio.netty.native.workdir={native}/netty',
          '-Djava.net.preferIPv6Addresses=system','-cp',';'.join(json.loads((QA/'classpath.json').read_text())),
          'net.minecraftforge.bootstrap.ForgeBootstrap','--launchTarget','forge_client','--username',name,'--version','26.3-forge-66.1.8','--gameDir',str(directory),
          '--assetsDir',str(MC/'assets'),'--assetIndex','34','--uuid',str(uuid.uuid3(uuid.NAMESPACE_DNS,'OfflinePlayer:'+name)),
          '--accessToken','0','--versionType','release','--width','854','--height','480']
    # Java argument files avoid the Windows command-line length limit.
    argfile=directory/'launch.args'
    argfile.write_text('\n'.join('"'+a.replace('\\','/').replace('"','\\"')+'"' for a in args), encoding='utf-8')
    log=(directory/'console.log').open('w',encoding='utf-8')
    child=subprocess.Popen([str(JAVA),'@'+str(argfile)],cwd=directory,stdout=log,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
    (directory/'pid.txt').write_text(str(child.pid))
    print(name,child.pid)

def rcon(command):
    def send(sock, ident, kind, value):
        body=struct.pack('<ii',ident,kind)+value.encode()+b'\0\0'
        sock.sendall(struct.pack('<i',len(body))+body)
    def receive(sock):
        def exact(n):
            data=b''
            while len(data)<n:
                chunk=sock.recv(n-len(data))
                if not chunk: raise ConnectionError('RCON closed')
                data+=chunk
            return data
        size=struct.unpack('<i',exact(4))[0]
        data=exact(size)
        return struct.unpack('<ii',data[:8]), data[8:-2].decode('utf-8',errors='replace')
    with socket.create_connection(('127.0.0.1',25585),timeout=5) as sock:
        send(sock,1,3,'windcraft-local-qa')
        for _ in range(2):
            header,_=receive(sock)
            if header[1]==2:
                if header[0]==-1: raise RuntimeError('RCON auth failed')
                break
        send(sock,2,2,command)
        print(receive(sock)[1])

if __name__=='__main__':
    parser=argparse.ArgumentParser(); parser.add_argument('action',choices=['prepare','client-a','client-b','command']); parser.add_argument('command',nargs='?'); args=parser.parse_args()
    if args.action=='prepare': prepare()
    elif args.action=='command': rcon(args.command)
    else: start_client('WCTestA' if args.action=='client-a' else 'WCTestB')
