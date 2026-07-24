#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为所有 MD 文件生成 title 和 description
"""

import os
import re
import glob
from pathlib import Path

# 定义文件名/路径到 title/description 的映射规则
def generate_metadata(file_path):
    """根据文件路径生成 title 和 description"""
    
    relative_path = file_path.replace('d:\\sp42\\code\\ajaxjs2\\aj-util\\docs-src\\src\\', '').replace('.md', '')
    file_name = os.path.basename(file_path).replace('.md', '')
    
    # 特殊映射规则
    rules = {
        'index': {
            'title': 'AJ Utilities Documentation',
            'description': 'Lightweight Java utility library (90KB JAR) with powerful components for file I/O, HTTP requests, encryption, and more'
        },
        'cn': {
            'title': 'AJ Utilities 简介',
            'description': '小型、干净、简单的 Java 工具库，JAR 包约 60KB，包含 IO、HTTP、加密等多种实用组件'
        },
        'README': {
            'title': 'Documentation Source Guide',
            'description': 'Eleventy-based documentation site setup guide with Less.js support'
        },
        'prompt': {
            'title': 'AI Prompts for Documentation',
            'description': 'AI prompts for generating tutorials and documentation for UrlEncode, HashHelper, XmlHelper, and BytesHelper'
        },
    }
    
    # 检查直接匹配
    if file_name in rules:
        return rules[file_name]
    
    # 目录映射规则
    dir_rules = {
        'io': {
            'title_prefix': '',
            'title_suffix': ' Tutorial',
            'desc_prefix': 'Utility methods for ',
            'desc_suffix': ' operations'
        },
        'reflect': {
            'title_prefix': '',
            'title_suffix': ' Tutorial',
            'desc_prefix': 'Utility methods for ',
            'desc_suffix': ' with reflection'
        },
        'date': {
            'title_prefix': 'Date ',
            'title_suffix': '',
            'desc_prefix': 'Methods for date ',
            'desc_suffix': ' operations'
        },
        'http_request': {
            'title_prefix': 'HTTP ',
            'title_suffix': ' Request',
            'desc_prefix': 'Methods for making HTTP ',
            'desc_suffix': ' requests'
        },
        'cryptography': {
            'title_prefix': '',
            'title_suffix': '',
            'desc_prefix': 'Methods for ',
            'desc_suffix': ' encryption and decryption'
        },
        'common': {
            'title_prefix': '',
            'title_suffix': '',
            'desc_prefix': 'Utility methods for ',
            'desc_suffix': ''
        },
    }
    
    # 特殊类名映射
    class_name_map = {
        'FileHelper': 'File Operations',
        'ZipHelper': 'ZIP Compression',
        'Resources': 'Resource Loading',
        'StreamHelper': 'Stream Operations',
        'Clazz': 'Class Reflection',
        'Methods': 'Method Reflection',
        'Types': 'Type Reflection',
        'Fields': 'Field Reflection',
        'StrUtil': 'String Utilities',
        'JsonUtil': 'JSON Utilities',
        'XmlHelper': 'XML Processing',
        'MapTool': 'Map Utilities',
        'ConvertBasicValue': 'Type Conversion',
        'BytesHelper': 'Byte Array Utilities',
        'MessageDigestHelper': 'Message Digest',
        'RandomTools': 'Random Generation',
        'RegExpUtils': 'Regular Expressions',
        'EncodeTools': 'Encoding Utilities',
        'Base64Helper': 'Base64 Encoding',
        'ObjectHelper': 'Object Utilities',
        'BoxLogger': 'Logging Utilities',
        'AesCrypto': 'AES Encryption',
        'RsaCrypto': 'RSA Encryption',
        'Get': 'GET',
        'Post': 'POST',
        'Put': 'PUT',
        'Delete': 'DELETE',
        'Head': 'HEAD',
        'Base': 'Request Base',
        'advanced-usage': 'Advanced Usage',
        'intro': 'Introduction',
        'convert': 'Date Conversion',
        'formatter': 'Date Formatting',
        'flow': 'Cryptographic Flow',
    }
    
    # 获取目录名
    dir_name = os.path.dirname(relative_path)
    
    # 获取类名（处理中英文文件）
    base_name = file_name
    if '-cn' in base_name:
        base_name = base_name.replace('-cn', '')
    
    # 构建 title
    if base_name in class_name_map:
        title_base = class_name_map[base_name]
    else:
        # 尝试从路径推断
        title_base = base_name
    
    # 确定语言
    is_chinese = '-cn' in file_name or 'cn' in relative_path.lower()
    
    if is_chinese:
        title_suffix = '教程' if dir_name != 'http_request' else ''
        title = f"{title_base}{title_suffix}"
    else:
        title_suffix = ' Tutorial' if dir_name != 'http_request' else ''
        title = f"{title_base}{title_suffix}"
    
    # 构建 description
    if dir_name in dir_rules:
        rule = dir_rules[dir_name]
        if is_chinese:
            desc_base = class_name_map.get(base_name, base_name)
            desc_action_map = {
                'File Operations': '文件读写、复制、移动、删除等',
                'ZIP Compression': 'ZIP 压缩和解压',
                'Resource Loading': '从 classpath 加载资源文件',
                'Stream Operations': '流式数据处理',
                'Class Reflection': '类加载和实例创建',
                'Method Reflection': '方法调用和查找',
                'Type Reflection': '泛型类型处理',
                'Field Reflection': '字段访问',
                'String Utilities': '字符串模板、填充、连接',
                'JSON Utilities': 'JSON 解析和生成',
                'XML Processing': 'XML 解析和处理',
                'Map Utilities': 'Map 工具方法',
                'Type Conversion': '基本类型转换',
                'Byte Array Utilities': '字节数组处理',
                'Message Digest': 'MD5/SHA 哈希计算',
                'Random Generation': '随机数和字符串生成',
                'Regular Expressions': '正则表达式工具',
                'Encoding Utilities': 'URL/Base64 编码解码',
                'Base64 Encoding': 'Base64 编解码',
                'Object Utilities': '对象工具方法',
                'Logging Utilities': '日志工具类',
                'AES Encryption': 'AES 加密解密',
                'RSA Encryption': 'RSA 加密解密',
                'GET': 'HTTP GET 请求',
                'POST': 'HTTP POST 请求',
                'PUT': 'HTTP PUT 请求',
                'DELETE': 'HTTP DELETE 请求',
                'HEAD': 'HTTP HEAD 请求',
                'Request Base': 'HTTP 请求基础类',
                'Advanced Usage': 'HTTP 请求高级用法',
                'Introduction': '日期处理简介',
                'Date Conversion': '日期类型转换',
                'Date Formatting': '日期格式化',
                'Cryptographic Flow': '加密流程说明',
            }
            description = desc_action_map.get(desc_base, f'{desc_base}相关操作')
        else:
            desc_base = title_base
            description = f"{rule['desc_prefix']}{desc_base.lower()}{rule['desc_suffix']}"
    else:
        # 通用描述
        if is_chinese:
            description = f"{title_base}的使用方法和示例"
        else:
            description = f"Tutorial for {title_base} with usage examples"
    
    return {'title': title, 'description': description}


def process_file(file_path):
    """处理单个 MD 文件"""
    
    # 读取文件内容
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 提取现有的 front matter
    front_matter_match = re.match(r'^---\s*\n(.*?)\n---\s*\n', content, re.DOTALL)
    
    if not front_matter_match:
        print(f"⚠️  No front matter found in {file_path}")
        return
    
    existing_front_matter = front_matter_match.group(1)
    body_content = content[front_matter_match.end():]
    
    # 生成新的 title 和 description
    metadata = generate_metadata(file_path)
    
    # 解析现有的 front matter 并保留其他字段
    new_front_matter_lines = []
    
    # 添加 title
    new_front_matter_lines.append(f"title: {metadata['title']}")
    
    # 尝试保留原有的 subTitle, date 等字段
    for line in existing_front_matter.split('\n'):
        line = line.strip()
        if line.startswith('subTitle:') or line.startswith('date:') or line.startswith('layout:'):
            new_front_matter_lines.append(line)
    
    # 添加 description
    new_front_matter_lines.append(f"description: {metadata['description']}")
    
    # 保留原有的 tags
    in_tags = False
    tags_content = []
    for line in existing_front_matter.split('\n'):
        line = line.rstrip()
        if line.startswith('tags:'):
            in_tags = True
            tags_content.append(line)
        elif in_tags:
            if line.startswith('  - '):
                tags_content.append(line)
            else:
                in_tags = False
    
    if tags_content:
        new_front_matter_lines.extend(tags_content)
    
    # 组合新的 front matter
    new_front_matter = '\n'.join(new_front_matter_lines)
    
    # 组合新内容
    new_content = f"---\n{new_front_matter}\n---\n{body_content}"
    
    # 写入文件
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    relative_path = file_path.replace('d:\\sp42\\code\\ajaxjs2\\aj-util\\docs-src\\src\\', '')
    print(f"✅ {relative_path}")
    print(f"   Title: {metadata['title']}")
    print(f"   Description: {metadata['description'][:60]}...")


def main():
    """主函数"""
    
    # 查找所有 MD 文件
    base_dir = r'd:\sp42\code\ajaxjs2\aj-util\docs-src\src'
    md_files = glob.glob(os.path.join(base_dir, '**', '*.md'), recursive=True)
    
    print(f"Found {len(md_files)} MD files to process\n")
    
    for file_path in sorted(md_files):
        try:
            process_file(file_path)
        except Exception as e:
            print(f"❌ Error processing {file_path}: {e}")
    
    print(f"\n✅ All {len(md_files)} files processed!")


if __name__ == '__main__':
    main()
